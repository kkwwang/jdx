package cn.yiidii.jdx.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.yiidii.jdx.config.prop.SystemConfigProperties;
import cn.yiidii.jdx.model.R;
import cn.yiidii.jdx.model.dto.AdminNotifyEvent;
import cn.yiidii.jdx.service.QLService;
import cn.yiidii.jdx.util.WXPushUtil;
import com.alibaba.fastjson.JSONObject;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ThirdCallbackController
 *
 * @author ed w
 * @since 1.0
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/third")
@RequiredArgsConstructor
public class ThirdCallbackController {

    private final SystemConfigProperties systemConfigProperties;
    private final QLService qlService;

    /**
     * wxPusher订阅回调，可用于给管理员推送
     *
     * @param paramJo 参数
     * @return R
     */
    @PostMapping("/wxPusher/follow/callback")
    public R<?> aa(@RequestBody JSONObject paramJo) {
        log.debug(StrUtil.format("wxPusher 事件回调: {}", paramJo.toJSONString()));
        String action = paramJo.getString("action");
        JSONObject dataJo = paramJo.getJSONObject("data");
        String extra = dataJo.getString("extra");
        JSONObject extraJo = JSONObject.parseObject(extra);
        if (!extraJo.containsKey("ptPin")) {
            return R.failed();
        }
        String ptPin = extraJo.getString("ptPin");
        String uid = dataJo.getString("uid");
        String appName = dataJo.getString("appName");
        switch (action) {
            case "app_subscribe": {
                qlService.bindWxPushUidToRemark(ptPin, uid);
                // 通知用户
                WXPushUtil.send(systemConfigProperties.getWxPusherAppToken(), Arrays.asList(uid), "关注成功", StrUtil.format("京东账户: {}关联成功，请关注后续通知", ptPin), "1");
                // 通知管理员
                SpringUtil.publishEvent(
                        new AdminNotifyEvent(
                                null,
                                "订阅通知",
                                StrUtil.format("pt_pin: {} 订阅了{}",
                                        ptPin,
                                        appName
                                ),
                                true
                        )
                );
                break;
            }
            default: {

                break;
            }
        }
        return R.ok();
    }

    /**
     * 青龙任务执行后的通知，来自sendNotify.js
     *
     * @param paramJo 参数
     * @return R
     */
    @PostMapping("/qlNotify")
    public R<?> qlNotify(@RequestBody JSONObject paramJo) {
        log.debug(StrUtil.format("青龙任务执行后通知, 参数: {}", paramJo.toJSONString()));
        return R.ok(null, "调用成功");
    }


}
