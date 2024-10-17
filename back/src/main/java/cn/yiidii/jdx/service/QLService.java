package cn.yiidii.jdx.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.yiidii.jdx.config.prop.SystemConfigProperties;
import cn.yiidii.jdx.config.prop.SystemConfigProperties.QLConfig;
import cn.yiidii.jdx.model.dto.AdminNotifyEvent;
import cn.yiidii.jdx.model.dto.RemarkInfo;
import cn.yiidii.jdx.model.ex.BizException;
import cn.yiidii.jdx.support.ITask;
import cn.yiidii.jdx.util.JDXUtil;
import cn.yiidii.jdx.util.ScheduleTaskUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * QLService
 *
 * @author ed w
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QLService implements ITask {

    private final static Map<String, String> QL_TOKEN_CACHE = new ConcurrentHashMap<>(16);

    private final SystemConfigProperties systemConfigProperties;
    private final ScheduleTaskUtil scheduleTaskUtil;

    public JSONObject submitCk(String cookie, String mobile) throws Exception {
        // 同步下数据
        this.refreshQLUsedCookieCount();
        List<QLConfig> availableQlConfigs = systemConfigProperties.getQls().stream().filter(ql -> ql.getDisabled() == 0 && ql.getUsed() < ql.getMax()).collect(Collectors.toList());
        if (CollUtil.isEmpty(availableQlConfigs)) {
            throw new BizException("无可用节点");
        }
        String ptPin = JDXUtil.getPtPinFromCK(cookie);


        List<QLAllNodeSearchResult> qlAllNodeSearchResults = this.searchEnvFromAllNode(ptPin, "JD_COOKIE");
        log.debug("[提交cookie], 所有节点搜索{}, 结果: {}", ptPin, JSON.toJSONString(qlAllNodeSearchResults));
        if (CollUtil.isEmpty(qlAllNodeSearchResults)) {
            // 随机一个节点
            QLConfig qlConfig = availableQlConfigs.get(RandomUtil.randomInt(availableQlConfigs.size()));
            log.debug("[提交cookie], pt_pin: {}, 在节点{}新增", ptPin, qlConfig.getDisplayName());
            // 保存并启用
            this.saveAndEnableEnv(qlConfig, "JD_COOKIE", cookie, "", mobile);
        } else {
            // 更新
            List<String> updateNodesNames = qlAllNodeSearchResults.stream().map(e -> e.getQlConfig().getDisplayName()).collect(Collectors.toList());
            log.debug("[提交cookie], pt_pin: {}, 在一下节点更新: {}", ptPin, JSON.toJSONString(updateNodesNames));
            qlAllNodeSearchResults.forEach(r -> r.getEnvs().forEach(e -> this.saveAndEnableEnv(r.getQlConfig(), "JD_COOKIE", cookie, e.getString("remarks"), mobile)));
        }

        // 生成动态二维码
//        JSONObject param = new JSONObject();
//        param.put("ptPin", ptPin);
//        String dynamicQR = WXPushUtil.getDynamicQR(systemConfigProperties.getWxPusherAppToken(), param);
        JSONObject result = new JSONObject();
//        result.put("dynamicWxPusherQRCode", dynamicQR);
        return result;
    }

    public void saveAndEnableEnv(QLConfig qlConfig, String name, String value, String remark, String mobile) {
        String displayName = qlConfig.getDisplayName();
        // 获取存在的env
        String ptPin = JDXUtil.getPtPinFromCK(value);
        JSONObject existEnv = this.getExistCK(qlConfig, ptPin);

        RemarkInfo remarkInfo = new RemarkInfo();


        if (StringUtils.hasText(remark) && JSONObject.isValidObject(remark)) {
            remarkInfo = JSONObject.parseObject(remark, RemarkInfo.class);
        }


        remarkInfo
                .setMobile(mobile)
                .setPtPin(ptPin);
        // todo 查询用户信息，组装remark
        if (!StringUtils.hasText(remarkInfo.getNotifyMobile())) {
            remarkInfo.setNotifyMobile(mobile);
        }

        HttpResponse userInfoResponse = HttpRequest.get("https://me-api.jd.com/user_new/info/GetJDUserInfoUnion")
                .cookie(value)
                .execute();



        if (userInfoResponse.getStatus() == HttpStatus.HTTP_OK) {
            String body = userInfoResponse.body();
            JSONObject jsonObject = JSON.parseObject(body);
            if (jsonObject.getIntValue("retcode") == 0) {
                JSONObject data = jsonObject.getJSONObject("data");
                String nickname = data.getJSONObject("userInfo").getJSONObject("baseInfo").getString("nickname");
                remarkInfo.setNickname(nickname);
            }
        }


        log.info("用户信息：{}", JSONObject.toJSONString(remarkInfo, SerializerFeature.WriteNullStringAsEmpty));
        remark = JSONObject.toJSONString(remarkInfo, SerializerFeature.WriteNullStringAsEmpty);

        // 推送青龙
        if (existEnv.isEmpty()) {
            // 新增逻辑
            JSONObject envJo = new JSONObject();
            envJo.put("name", name);
            envJo.put("value", value);
            envJo.put("remarks", remark);
            JSONArray paramJa = new JSONArray();
            paramJa.add(envJo);
            try {
                log.debug(StrUtil.format("[青龙 - {}] 添加环境变量, 参数: {}", displayName, envJo.toJSONString()));
                HttpResponse response = HttpRequest.post(qlConfig.getUrl().concat("open/envs"))
                        .bearerAuth(this.getQLToken(displayName))
                        .body(paramJa.toJSONString())
                        .execute();
                log.debug(StrUtil.format("[青龙 - {}] 添加环境变量, 响应: {}", displayName, response.body()));
            } catch (Exception e) {
                log.error(StrUtil.format("[青龙 - {}] 添加环境变量发生异常: {}", displayName, e));
                throw new BizException("连接青龙发生异常, 请联系系统管理员");
            }
        } else {
            // 更新并且启用
            // 更新
            JSONObject envJo = new JSONObject();
            envJo.put("name", name);
            envJo.put("value", value);
            envJo.put("remarks", remark);
            envJo.put("id", existEnv.getString("id"));
            try {
                // 更新
                this.updateEnv(qlConfig, envJo);
                // 启用
                if (existEnv.getInteger("status") == 1) {
                    this.enableEnv(qlConfig, Collections.singletonList(existEnv.getString("id")));
                }
            } catch (Exception e) {
                log.error(StrUtil.format("[青龙 - {}] 更新并启用环境变量发生异常: {}", displayName, e));
                throw new BizException("连接青龙发生异常, 请联系系统管理员");
            }
        }

        String text = StrUtil.format("{} 登录成功", StringUtils.hasText(remarkInfo.getWechat()) ? remarkInfo.getWechat() : remarkInfo.getNickname());

        SpringUtil.publishEvent(
                new AdminNotifyEvent(
                        Collections.singletonList(mobile),
                        "【通知】",
                        text,
                        false
                )
        );


    }

    public void bindWxPushUidToRemark(String ptPin, String uid) {
        List<QLConfig> qlConfigs = systemConfigProperties.getQls();
        qlConfigs.forEach(qlConfig -> {
            List<JSONObject> envs = this.searchEnv(qlConfig, ptPin);
            if (CollUtil.isEmpty(envs)) {
                return;
            }
            envs.forEach(env -> {
                String remark = env.getString("remarks");
                Set<String> newRemarkSplit = StrUtil.split(remark, "@@").stream().filter(e -> !StrUtil.startWith(e, "UID_") && StrUtil.isNotBlank(e)).collect(Collectors.toSet());
                newRemarkSplit.add(uid);
                if (newRemarkSplit.size() <= 1) {
                    newRemarkSplit.add(ptPin);
                }
                String newRemark = CollUtil.join(newRemarkSplit, "@@");
                env.put("remarks", newRemark);
                this.updateEnv(qlConfig, env);
            });
        });
    }

    /**
     * 搜索环境变量
     *
     * @param qlConfig    qlConfig
     * @param searchValue searchValue
     * @param status      0：正常；1：禁用
     * @return
     */
    public List<JSONObject> searchEnv(QLConfig qlConfig, String searchValue, Integer status) {
        List<JSONObject> envs = this.searchEnv(qlConfig, searchValue);
        return envs.stream().filter(e -> e.getInteger("status").compareTo(status) == 0).collect(Collectors.toList());
    }

    public List<JSONObject> searchEnv(QLConfig qlConfig, String searchValue) {
        String displayName = qlConfig.getDisplayName();
        try {
            HttpResponse response = HttpRequest.get(StrUtil.format("{}open/envs?searchValue={}", qlConfig.getUrl(), searchValue))
                    .bearerAuth(this.getQLToken(displayName))
                    .execute();
            log.debug(StrUtil.format("[青龙 - {}] 搜索环境变量, 参数: {}, 响应: {}", displayName, searchValue, response.body()));
            JSONObject respJo = JSONObject.parseObject(response.body());
            JSONArray data = respJo.getJSONArray("data");

            // 返回
            return data.stream().map(d -> {
                JSONObject tmp = (JSONObject) d;
                JSONObject jo = new JSONObject();
                String _id = tmp.getString("_id");
                String id = tmp.getString("id");
                jo.put("id", StrUtil.isNotBlank(_id) ? _id : StrUtil.isNotBlank(id) ? id : "");
                jo.put("name", tmp.getString("name"));
                jo.put("value", tmp.getString("value"));
                jo.put("status", tmp.getInteger("status"));
                jo.put("remarks", tmp.getString("remarks"));
                return jo;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.debug(StrUtil.format("连接青龙发生异常, e: {}", e));
            throw new BizException("连接青龙发生异常, 请联系系统管理员" + displayName);
        }
    }

    public List<QLAllNodeSearchResult> searchEnvFromAllNode(String searchValue, String name) {
        List<QLConfig> qlConfigs = systemConfigProperties.getQls();
        ThreadPoolTaskExecutor asyncExecutor = SpringUtil.getBean("asyncExecutor", ThreadPoolTaskExecutor.class);
        List<CompletableFuture<QLAllNodeSearchResult>> futures =
                qlConfigs.stream().map(ql -> CompletableFuture.supplyAsync(() -> {
                    List<JSONObject> envs = this.searchEnv(ql, searchValue);
                    if (StrUtil.isNotBlank(name)) {
                        envs = envs.stream().filter(e -> StrUtil.equalsIgnoreCase(e.getString("name"), name)).collect(Collectors.toList());
                    }
                    return new QLAllNodeSearchResult(ql, envs);
                }, asyncExecutor)).collect(Collectors.toList());
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
        CompletableFuture<List<QLAllNodeSearchResult>> finalResults = allFutures.thenApply(v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
        try {
            return finalResults.get().stream().filter(r -> r.getEnvs().size() > 0).collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void updateEnv(QLConfig qlConfig, JSONObject envJo) {
        envJo.remove("status");
        String id = envJo.getString("id");
        String displayName = qlConfig.getDisplayName();
        // 第一次，用_id
        envJo.remove("id");
        envJo.put("_id", id);
        log.debug(StrUtil.format("[青龙 - {}] 第一次尝试更新环境变量, 参数: {}", displayName, envJo.toJSONString()));
        HttpResponse response = HttpRequest.put(qlConfig.getUrl().concat("open/envs"))
                .bearerAuth(this.getQLToken(displayName))
                .body(envJo.toJSONString())
                .execute();
        log.debug(StrUtil.format("[青龙 - {}] 更新环境变量, 状态码: {}, 响应: {}", displayName, response.getStatus(), response.body()));

        // 如果第一次异常，第二次用id
        if (response.getStatus() == HttpStatus.HTTP_INTERNAL_ERROR || response.getStatus() == HttpStatus.HTTP_BAD_REQUEST) {
            envJo.remove("_id");
            envJo.put("id", id);
            log.debug(StrUtil.format("[青龙 - {}] 第二次尝试更新环境变量, 参数: {}", displayName, envJo.toJSONString()));
            response = HttpRequest.put(qlConfig.getUrl().concat("open/envs"))
                    .bearerAuth(this.getQLToken(displayName))
                    .body(envJo.toJSONString())
                    .execute();
            log.debug(StrUtil.format("[青龙 - {}] 第二次尝试更新环境变量, 状态码: {}, 响应: {}", displayName, response.getStatus(), response.body()));
        }
        if (response.getStatus() == HttpStatus.HTTP_INTERNAL_ERROR || response.getStatus() == HttpStatus.HTTP_BAD_REQUEST) {
            throw new BizException("更新失败，请联系系统管理员");
        }
    }

    public void enableEnv(QLConfig qlConfig, List<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        String displayName = qlConfig.getDisplayName();
        try {
            log.debug(StrUtil.format("[青龙 - {}] 启用环境变量, 参数: {}", displayName, JSON.toJSONString(ids)));
            HttpResponse response = HttpRequest.put(qlConfig.getUrl().concat("open/envs/enable"))
                    .bearerAuth(this.getQLToken(displayName))
                    .body(JSON.toJSONString(ids))
                    .execute();
            log.debug(StrUtil.format("[青龙 - {}] 启用环境变量, 响应: {}", displayName, response.body()));
        } catch (Exception e) {
            log.debug(StrUtil.format("连接青龙发生异常, e: {}", e));
            throw new BizException("连接青龙发生异常, 请联系系统管理员");
        }
    }

    public void disableEnv(QLConfig qlConfig, List<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        String displayName = qlConfig.getDisplayName();
        try {
            log.debug(StrUtil.format("[青龙 - {}] 禁用环境变量, 参数: {}", displayName, JSON.toJSONString(ids)));
            HttpResponse response = HttpRequest.put(qlConfig.getUrl().concat("open/envs/disable"))
                    .bearerAuth(this.getQLToken(displayName))
                    .body(JSON.toJSONString(ids))
                    .execute();
            log.debug(StrUtil.format("[青龙 - {}] 禁用环境变量, 响应: {}", displayName, response.body()));
        } catch (Exception e) {
            log.debug(StrUtil.format("连接青龙发生异常, e: {}", e));
            throw new BizException("连接青龙发生异常, 请联系系统管理员");
        }
    }

    public void refreshQLUsedCookieCount() {
        List<QLConfig> qlConfigs = systemConfigProperties.getQls();
        ThreadPoolTaskExecutor asyncExecutor = SpringUtil.getBean("asyncExecutor", ThreadPoolTaskExecutor.class);
        CountDownLatch countDownLatch = new CountDownLatch(qlConfigs.size());
        qlConfigs.forEach(ql -> asyncExecutor.execute(() -> {
            try {
                List<JSONObject> normalEnvs = this.searchEnv(ql, "JD_COOKIE", 0);
                ql.setUsed(normalEnvs.size());
                if (ql.getUsed() >= ql.getMax()) {
                    ql.setDisabled(1);
                }
            } catch (Exception e) {
                log.error(StrUtil.format("refreshQLUsedCookieCount err: {}", e));
            } finally {
                countDownLatch.countDown();
            }
        }));
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error(StrUtil.format("refreshQLUsedCookieCount err wher countDownLatch await: {}", e));
        }
    }

    public synchronized String getQLToken(String displayName) {
        QLConfig qlConfig = systemConfigProperties.getQLConfigByDisplayName(displayName);
        if (Objects.isNull(qlConfig)) {
            throw new BizException(StrUtil.format("青龙节点【{}】不存在", displayName));
        }
        String token = QL_TOKEN_CACHE.get(qlConfig.getClientId());
        if (StrUtil.isNotBlank(token)) {
            return token;
        }
        return this.refreshToken(qlConfig);
    }

    public String refreshToken(QLConfig ql) {
        HttpResponse response = HttpRequest.get(StrUtil.format("{}open/auth/token?client_id={}&client_secret={}", ql.getUrl(), ql.getClientId(), ql.getClientSecret())).execute();
        if (response.getStatus() == HttpStatus.HTTP_OK) {
            JSONObject respJo = JSONObject.parseObject(response.body());
            Integer code = respJo.getInteger("code");
            if (code == HttpStatus.HTTP_OK) {
                String token = respJo.getJSONObject("data").getString("token");
                QL_TOKEN_CACHE.put(ql.getClientId(), token);
                return token;
            }
        }
        return null;
    }

    @Override
    public void startTimerTask() {
        scheduleTaskUtil.startCron("QL_timerRefreshToken", () -> this.timerRefreshToken(), "0 0 0/1 * * ?");
    }

    private void timerRefreshToken() {
        List<QLConfig> qlConfigs = systemConfigProperties.getQls();
        qlConfigs.forEach(this::refreshToken);
    }

    /**
     * 获取存在的CK
     *
     * @return JSONObject
     */
    private JSONObject getExistCK(QLConfig qlConfig, String ptPin) {
        log.debug(StrUtil.format("[青龙 - {}], 获取存在的Cookie: {}", qlConfig.getDisplayName(), ptPin));
        JSONObject result;
        List<JSONObject> envs = this.searchEnv(qlConfig, ptPin);
        if (envs.size() == 0) {
            result = new JSONObject();
        } else if (envs.size() == 1) {
            result = envs.get(0);
        } else {
            // 通过【pt_pin=xxx;】搜索出来有多个的话, 返回正常状态(status=0)的第一个， 如果没有正常的, 就返回禁用状态(status=1)的第一个
            Map<Integer, List<JSONObject>> statusMap = envs.stream().collect(Collectors.groupingBy(e -> e.getInteger("status")));
            if (statusMap.get(0).size() >= 1) {
                result = statusMap.get(0).get(0);
            } else if (statusMap.get(1).size() >= 1) {
                result = statusMap.get(1).get(0);
            } else {
                result = new JSONObject();
            }
        }
        log.debug(StrUtil.format("[青龙 - {}], 获取存在的Cookie: {}, 最终返回结果: {}", qlConfig.getDisplayName(), ptPin, result.toJSONString()));
        return result;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    public class QLAllNodeSearchResult {

        /**
         * 青龙配置
         */
        private QLConfig qlConfig;
        /**
         * 单个ql搜索到的环境变量
         */
        private List<JSONObject> envs;
    }
}
