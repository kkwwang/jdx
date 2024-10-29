package cn.yiidii.jdx.util;


import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.yiidii.jdx.config.prop.SystemConfigProperties;
import com.alibaba.fastjson.JSONObject;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class QywxPushUtil {

    private static final String PUSH_URL = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=${QYWX_KEY}";
    private static final String YY_PUSH_URL = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=ACCESS_TOKEN";


    public static void send(String qywxKey, String title, String content, Set<String> mobileList, Set<String> qywxUserIdList) {

        QywxUtil qywxUtil = SpringUtil.getBean(QywxUtil.class);
        SystemConfigProperties systemConfigProperties = SpringUtil.getBean(SystemConfigProperties.class);
        String contactsToken = qywxUtil.getContactsToken();


        JSONObject reqParamJo = new JSONObject();
        reqParamJo.put("msgtype", "text");
        reqParamJo.put("agentid", systemConfigProperties.getAgentid());
        reqParamJo.put("touser", String.join("|", qywxUserIdList));


        JSONObject text = new JSONObject();
        if (null != mobileList && !mobileList.isEmpty()) {
            text.put("mentioned_mobile_list", mobileList.stream().filter(StringUtils::hasText).collect(Collectors.toList()));
        }

        if (null != qywxUserIdList && !qywxUserIdList.isEmpty()) {
            text.put("mentioned_list", qywxUserIdList.stream().filter(StringUtils::hasText).collect(Collectors.toList()));
        }

        text.put("content", title + "\n\n" + content);
        reqParamJo.put("text", text);


        log.info(StrUtil.format("企业微信发送消息, 参数: {}", reqParamJo.toJSONString()));
        @Cleanup HttpResponse respYY = HttpRequest.post(YY_PUSH_URL.replace("ACCESS_TOKEN", contactsToken))
                .body(reqParamJo.toJSONString())
                .execute();
        @Cleanup HttpResponse resp = HttpRequest.post(PUSH_URL.replace("${QYWX_KEY}", qywxKey))
                .body(reqParamJo.toJSONString())
                .execute();
        log.info(StrUtil.format("企业微信应用发送消息, 响应: {}", respYY.body()));
        log.info(StrUtil.format("企业微信群发送消息, 响应: {}", resp.body()));
    }


}
