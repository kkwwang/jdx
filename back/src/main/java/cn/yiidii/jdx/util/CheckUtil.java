package cn.yiidii.jdx.util;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.yiidii.jdx.config.prop.SystemConfigProperties;
import cn.yiidii.jdx.model.dto.RemarkInfo;
import cn.yiidii.jdx.service.QLService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckUtil {

    private final SystemConfigProperties systemConfigProperties;

    public Set<String> checkBindQywx(String mobile) {
        Set<String> result = new HashSet<>();
        // 获取token
        String token = getToken();
        if (null == token) {
            return result;
        }

        JSONObject env = getEnv(mobile);
        String remark = env.getString("remarks");

        RemarkInfo remarkInfo = JSONObject.parseObject(remark, RemarkInfo.class);

        Set<String> notifyMobileSet = remarkInfo.getNotifyMobile();
        notifyMobileSet.add(mobile);

        Set<String> qywxUserIdSet = remarkInfo.getQywxUserId();

        // 获取用户id
        for (String notifMobile : notifyMobileSet) {
            // 手机号获取userid
            String notifyUserid = getUserid(notifMobile, token);
            if (StringUtils.hasText(notifyUserid)) {
                notifyUserid = getUserinfo(notifyUserid, token);
                if (StringUtils.hasText(notifyUserid)) {
                    qywxUserIdSet.add(notifyUserid);
                }
            }
        }

        // 验证有效性
        for (String qywxUserId : qywxUserIdSet) {
            if (StringUtils.hasText(qywxUserId)) {
                qywxUserId = getUserinfo(qywxUserId, token);
                if (StringUtils.hasText(qywxUserId)) {
                    result.add(qywxUserId);
                }
            }
        }


        return result;
    }

    public boolean envIsEnable(String mobile) {
        JSONObject env = getEnv(mobile);
        String niceName = JDXUtil.getNiceName(env.getString("value"));
        return StringUtils.hasText(niceName);
    }

    public JSONObject getEnv(String mobile) {
        // 用手机号匹配到青龙账号
        QLService service = SpringUtil.getBean(QLService.class);
        List<JSONObject> envs = service.searchEnv(mobile).stream().filter(item -> StringUtils.pathEquals("JD_COOKIE", item.getString("name"))).collect(Collectors.toList());
        for (JSONObject env : envs) {
            String remark = env.getString("remarks");



            RemarkInfo remarkInfo = JSONObject.parseObject(remark, RemarkInfo.class);
            if (StringUtils.pathEquals(mobile, remarkInfo.getMobile())) {
                return env;
            }
        }
        return null;
    }

    private String getToken() {
        String corpid = systemConfigProperties.getCorpid();
        String corpsecret = systemConfigProperties.getCorpsecret();

        String getToken = SpringUtil.getProperty("qywx.getToken");

        // 获取token
        @Cleanup HttpResponse response = HttpRequest.get(getToken.replace("ID", corpid).replace("SECRET", corpsecret))
                .execute();
        if (response.getStatus() == HttpStatus.HTTP_OK) {
            String body = response.body();
            JSONObject jsonObject = JSON.parseObject(body);
            if (jsonObject.getInteger("errcode") == 0) {
                return jsonObject.getString("access_token");
            }
        }
        return null;
    }

    private String getUserid(String mobile, String token) {
        String getuserid = SpringUtil.getProperty("qywx.getuserid");
        JSONObject reqParamJo = new JSONObject();
        reqParamJo.put("mobile", mobile);
        // 获取token
        @Cleanup HttpResponse response = HttpRequest.post(getuserid.replace("ACCESS_TOKEN", token))
                .body(reqParamJo.toJSONString())
                .execute();

        if (response.getStatus() == HttpStatus.HTTP_OK) {
            String body = response.body();
            JSONObject jsonObject = JSON.parseObject(body);
            return jsonObject.getString("userid");
        }
        return null;
    }

    private String getUserinfo(String userid, String token) {
        String getuserinfo = SpringUtil.getProperty("qywx.getuserinfo");
        // 获取token
        @Cleanup HttpResponse response = HttpRequest.get(getuserinfo.replace("ACCESS_TOKEN", token).replace("USERID", userid))
                .execute();

        if (response.getStatus() == HttpStatus.HTTP_OK) {
            String body = response.body();
            JSONObject jsonObject = JSON.parseObject(body);
            if (jsonObject.getInteger("errcode") == 0 && jsonObject.getInteger("status") == 1) {
                return jsonObject.getString("userid");
            }
        }
        return null;
    }
}
