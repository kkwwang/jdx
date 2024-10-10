package cn.yiidii.jdx.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.yiidii.jdx.config.prop.SystemConfigProperties;
import cn.yiidii.jdx.config.prop.SystemConfigProperties.QLConfig;
import cn.yiidii.jdx.model.dto.AdminNotifyEvent;
import cn.yiidii.jdx.support.ITask;
import cn.yiidii.jdx.util.JDXUtil;
import cn.yiidii.jdx.util.ScheduleTaskUtil;
import cn.yiidii.jdx.util.WXPushUtil;
import cn.yiidii.jdx.util.jd.JDTaskUtil;
import cn.yiidii.jdx.util.jd.JDTaskUtil.CheckCookieResult;
import com.alibaba.fastjson.JSONObject;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

/**
 * JDTaskService
 *
 * @author ed w
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JDTaskService implements ITask {

    public static final String CFD_CONFIG_PATH = System.getProperty("user.dir") + File.separator + "config" + File.separator + "cfd" + File.separator + "config.json";

    private static Map<String, Object> cfdCache = new ConcurrentHashMap<>(16);

    private static Long cfdNextTime = 0L;

    private final SystemConfigProperties systemConfigProperties;
    private final QLService qlService;
    private final ScheduleTaskUtil scheduleTaskUtil;

    @PostConstruct
    public void init() {
        try {
            String configStr = FileUtil.readUtf8String(CFD_CONFIG_PATH);
            JSONObject jo = JSONObject.parseObject(configStr);
            BeanUtil.copyProperties(jo, cfdCache);
        } catch (IORuntimeException e) {
            cfdCache = new ConcurrentHashMap<>(16);
        }

        cfdNextTime = DateUtil.beginOfHour(DateUtil.offsetHour(new Date(), 1)).toJdkDate().getTime();
//        cfdNextTime = DateUtil.beginOfMinute(DateUtil.offsetMinute(new Date(), 2)).toJdkDate().getTime();
        log.debug(StrUtil.format("初始化财富岛下次时间为: {}", DateUtil.formatDateTime(new Date(cfdNextTime))));
    }

    public List<JSONObject> timerCheckCookie() {
        // 所有青龙节点
        List<QLConfig> qlConfigs = systemConfigProperties.getQls();
        ThreadPoolTaskExecutor asyncExecutor = SpringUtil.getBean("asyncExecutor", ThreadPoolTaskExecutor.class);
        // 最终结果
        List<JSONObject> result = new ArrayList<>();
        for (QLConfig qlConfig : qlConfigs) {
            // 单个QL下的可用的Cookie
            List<JSONObject> envs = qlService.searchEnv(qlConfig, "JD_COOKIE")
                    .stream().filter(e -> e.getInteger("status") == 0).collect(Collectors.toList());
            // 一个Cookie一个任务
            List<CompletableFuture<CheckCookieResult>> completableFutures = envs.stream().map(env -> CompletableFuture.supplyAsync(() -> {
                String value = env.getString("value");
                Thread.currentThread().setName(StrUtil.format("checkCookie_{}", JDXUtil.getPtPinFromCK(value)));
                CheckCookieResult checkCookieResult = JDTaskUtil.checkCookie(value);
                checkCookieResult.setId(env.getString("id"));
                if (checkCookieResult.isExpired()) {
                    // 通知到微信
                    String remarks = env.getString("remarks");
                    String uid = JDXUtil.getUidFromRemark(remarks);
                    if (StrUtil.isBlank(uid)) {
                        return checkCookieResult;
                    }
                    WXPushUtil.send(systemConfigProperties.getWxPusherAppToken(),
                            Arrays.asList(uid),
                            "Cookie失效通知",
                            StrUtil.format("{}, {}", checkCookieResult.getPtPin(), checkCookieResult.getRemark()),
                            "1");
                }
                return checkCookieResult;
            }, asyncExecutor)).collect(Collectors.toList());
            // 等待所有任务执行完成
            List<CheckCookieResult> checkCookieResults = completableFutures.stream().map(CompletableFuture::join).collect(Collectors.toList());
            List<String> expiredPtPins = checkCookieResults.stream()
                    .filter(CheckCookieResult::isExpired)
                    .map(CheckCookieResult::getPtPin)
                    .collect(Collectors.toList());
            List<String> ids = checkCookieResults.stream().filter(CheckCookieResult::isExpired).map(CheckCookieResult::getId).collect(Collectors.toList());
            try {
                // 禁用Cookie
                qlService.disableEnv(qlConfig, ids);
            } catch (Exception e) {
                log.error("定时检查cookie时, 禁用环境变量发生异常, displayName: {}", qlConfig.getDisplayName());
            }
            if (CollUtil.isNotEmpty(expiredPtPins)) {
                JSONObject jo = new JSONObject();
                jo.put("displayName", qlConfig.getDisplayName());
                jo.put("expiredPtPins", expiredPtPins);
                result.add(jo);
            }
        }

        String adminUid = systemConfigProperties.getWxPusherAdminUid();
        if (CollUtil.isNotEmpty(result) && StrUtil.isNotBlank(adminUid)) {
            String adminContent = result.stream().map(jo ->
                    StrUtil.format("节点【{}】以下Cookie已失效，已自动禁用\r\n{}",
                            jo.getString("displayName"),
                            CollUtil.join(jo.getJSONArray("expiredPtPins"), "\r\n")))
                    .collect(Collectors.joining("\r\n\r\n"));
            SpringUtil.publishEvent(
                    new AdminNotifyEvent(
                            null,
                            "Cookie失效通知",
                            adminContent,
                            true
                    )
            );
        }
        return result;
    }

    @Override
    public void startTimerTask() {
//        scheduleTaskUtil.startCron("jdTask_checkCookie", this::timerCheckCookie, "0 0 12 * * ?");
//        scheduleTaskUtil.startCron("jdTask_cfd", this::exchangeCfd, "0 59 * * * ?");
    }

}
