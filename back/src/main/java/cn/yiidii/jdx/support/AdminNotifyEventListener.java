package cn.yiidii.jdx.support;

import cn.hutool.core.util.StrUtil;
import cn.yiidii.jdx.config.prop.SystemConfigProperties;
import cn.yiidii.jdx.model.dto.AdminNotifyEvent;
import cn.yiidii.jdx.util.IdUtil;
import cn.yiidii.jdx.util.QywxPushUtil;
import cn.yiidii.jdx.util.WXPushUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 管理员通知事件监听
 *
 * @author ed w
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminNotifyEventListener implements ApplicationListener<AdminNotifyEvent> {

    private static final String PREFIX = "adminNotify";

    private final SystemConfigProperties systemConfigProperties;

    @Override
    @Async("asyncExecutor")
    public void onApplicationEvent(AdminNotifyEvent event) {
        Thread.currentThread().setName(String.format(Thread.currentThread().getName(), IdUtil.randomSnowflakeId(PREFIX) + "_"));
        WXPushUtil.send(systemConfigProperties.getWxPusherAppToken(),
                Arrays.asList(systemConfigProperties.getWxPusherAdminUid()),
                event.getTitle() + event.getContent(),
                event.getContent() + (null != event.getMobileList() && !event.getMobileList().isEmpty() ? "\n手机号：" + StringUtils.join(event.getMobileList(), ',') : ""),
                "1");

        if (!event.getAdminNotify()) {
            QywxPushUtil.send(
                    systemConfigProperties.getQywxKey(),
                    event.getTitle(),
                    event.getContent(),
                    event.getMobileList()
            );
        }

        log.debug(StrUtil.format("[admin通知], title: {}, content: {}", event.getTitle(), event.getContent()));
    }
}
