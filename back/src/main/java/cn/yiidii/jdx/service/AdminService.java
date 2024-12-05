package cn.yiidii.jdx.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.yiidii.jdx.config.prop.SystemConfigProperties;
import cn.yiidii.jdx.config.prop.SystemConfigProperties.QLConfig;
import cn.yiidii.jdx.model.dto.AdminNotifyEvent;
import cn.yiidii.jdx.model.dto.RemarkInfo;
import cn.yiidii.jdx.model.ex.BizException;
import cn.yiidii.jdx.util.QywxUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ed w
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final SystemConfigProperties systemConfigProperties;
    private final QLService qlService;
    private final QywxUtil qywxUtil;

    public List<QLConfig> getQLConfig() {
        return systemConfigProperties.getQls();
    }


    public List<QLConfig> saveQLConfig(QLConfig qlConfig) {
        String url = qlConfig.getUrl();
        url = url.startsWith("http") ? url : "http://".concat(url);
        url = url.endsWith("/") ? url : url.concat("/");
        qlConfig.setUrl(url);

        List<QLConfig> qls = systemConfigProperties.getQls();
//        QLConfig exist = systemConfigProperties.getQLConfigByDisplayName(qlConfig.getDisplayName());
//        if (Objects.isNull(exist)) {
        Map<String, QLConfig> qlConfigMap = qls.stream().collect(Collectors.toMap(QLConfig::getDisplayName, e -> e, (e1, e2) -> e2));
        qlConfigMap.put(qlConfig.getDisplayName(), qlConfig);
        qls = new ArrayList<>(qlConfigMap.values());
        systemConfigProperties.setQls(new ArrayList<>(qlConfigMap.values()));
//        } else {
//            qls.add(qlConfig);
//        }


        // 刷新下次数
        qlService.refreshQLUsedCookieCount();
        log.info(StrUtil.format("[admin] 更新ql: {}", JSON.toJSONString(qls)));
        return qls;
    }

    public List<QLConfig> delQLConfig(String displayName) {
        QLConfig exist = systemConfigProperties.getQLConfigByDisplayName(displayName);
        if (Objects.isNull(exist)) {
            throw new BizException("青龙配置不存在");
        }
        List<QLConfig> qls = systemConfigProperties.getQls();
        qls = qls.stream().filter(ql -> !StrUtil.equalsIgnoreCase(displayName, ql.getDisplayName())).collect(Collectors.toList());
        systemConfigProperties.setQls(qls);
        log.info(StrUtil.format("[admin] 删除ql: {}", displayName));
        return qls;
    }


    public JSONObject updateEnv(String mobile) {
        log.info("envs 修正开始");
        if (!StringUtils.hasText(mobile)) {
            mobile = "";
        }
        long start = System.currentTimeMillis();

        Map<String, Set<String>> qywxUserIdMap = new HashMap<>();

        List<QLService.QLAllNodeSearchResult> jdCookie = qlService.searchEnvFromAllNode(mobile, "JD_COOKIE");
        for (QLService.QLAllNodeSearchResult nodeSearchResult : jdCookie) {
            for (JSONObject env : nodeSearchResult.getEnvs()) {
                String remark = env.getString("remarks");
                try {

                    try {
                        JSONObject.parseObject(remark, RemarkInfo.class);
                    } catch (Exception e) {
                        try {
                            log.error("解析remark异常: {}", remark);
                            // todo 临时代码
                            RemarkInfo remarkInfo = JSONObject.parseObject(remark, RemarkInfo.class);
                            env.put("remarks", JSONObject.toJSONString(remarkInfo, SerializerFeature.WriteNullStringAsEmpty));
                            qlService.updateEnv(nodeSearchResult.getQlConfig(), env);
                        } catch (Exception ignored) {
                            throw new RuntimeException("参数配置错误，请检查: \n" + remark);
                        }
                    } finally {
                        try {
                            RemarkInfo remarkInfo = JSONObject.parseObject(remark, RemarkInfo.class);
                            remarkInfo.setNotifyMobile(remarkInfo.getMobile());
                            Set<String> bindQywx = qywxUtil.checkBindQywx(remarkInfo.getMobile());

                            if (bindQywx.isEmpty()) {
                                remarkInfo.setQywxUserId(new HashSet<>(Collections.singletonList(mobile)));
                                qywxUtil.createUser(mobile, new HashSet<>(Collections.singletonList(remarkInfo.getPtPin())));
                            } else {
                                remarkInfo.setQywxUserId(bindQywx);
                            }

                            if (!StringUtils.hasText(remarkInfo.getLoginTime())) {
                                remarkInfo.setLoginTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                            }

                            env.put("remarks", JSONObject.toJSONString(remarkInfo, SerializerFeature.WriteNullStringAsEmpty));
                            qlService.updateEnv(nodeSearchResult.getQlConfig(), env);


                            for (String qywxId : bindQywx) {
                                Set<String> ptPinSet = qywxUserIdMap.computeIfAbsent(qywxId, k -> new HashSet<>());
                                ptPinSet.add(remarkInfo.getPtPin());
                            }
                        } catch (Exception ignored) {
                            throw new RuntimeException("参数配置错误，请检查: \n" + remark);
                        }
                    }
                } catch (Exception e) {
                    SpringUtil.publishEvent(
                            new AdminNotifyEvent(
                                    new HashSet<>(),
                                    new HashSet<>(Collections.singletonList(systemConfigProperties.getQywxConfig().getAdminQywxId())),
                                    "【参数配置错误】",
                                    e.getMessage()
                            )
                    );
                }
            }
        }


        // 更新企业微信信息

        qywxUserIdMap.forEach(qywxUtil::updateQywxUserPosition);

        log.info("envs 修正更新成功，耗时：{} s", (System.currentTimeMillis() - start) / 1000);
        return new JSONObject();
    }

}
