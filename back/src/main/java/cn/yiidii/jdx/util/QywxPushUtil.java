package cn.yiidii.jdx.util;


import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSONObject;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class QywxPushUtil {

    private static final String PUSH_URL = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=${QYWX_KEY}";

    /**
     * json: {
     * msgtype: 'text',
     * text: {
     * content: `${text}\n\n${desp}`,
     * mentioned_mobile_list: mentioned_mobile_list
     * },
     * }
     */

    public void send(String qywxKey, String title, String content, List<String> mobileList) {
        JSONObject reqParamJo = new JSONObject();
        reqParamJo.put("msgtype", "text");
        JSONObject text = new JSONObject();
        if (null != mobileList && !mobileList.isEmpty()) {
            text.put("mentioned_mobile_list", mobileList);
            content += "\n\n手机号：" + StringUtils.join(mobileList.stream().map(item -> item.replaceAll("([0-9]{3})[0-9]{4}([0-9]{4})", "$1****$2")).collect(Collectors.toList()), '、');
        }

        text.put("content", title + "\n\n" + content);
        reqParamJo.put("text", text);


        log.debug(StrUtil.format("企业微信发送消息, 参数: {}", reqParamJo.toJSONString()));
        HttpResponse resp = HttpRequest.post(PUSH_URL.replace("${QYWX_KEY}", qywxKey))
                .body(reqParamJo.toJSONString())
                .execute();
        log.debug(StrUtil.format("企业微信发送消息, 响应: {}", resp.body()));
    }


}
