package cn.yiidii.jdx.util;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
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
public class QywxUtil {

    private final SystemConfigProperties systemConfigProperties;
    // 微信access_token有效期7200秒，提前200秒刷新
    TimedCache<String, String> timedCache = CacheUtil.newTimedCache(7000 * 1000);

    public Set<String> checkBindQywx(String mobile) {
        Set<String> result = new HashSet<>();
        // 获取token
        String token = getToken();
        if (null == token) {
            return result;
        }

        JSONObject env = getEnv(mobile);
        if (null == env) {
            return result;
        }
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
        if (env == null) {
            return false;
        }
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

    public String getToken() {
        return getToken(null);
    }

    public String getContactsToken() {
        String contactsSecret = systemConfigProperties.getContactsSecret();
        return getToken(contactsSecret);

    }

    public String getToken(String contactsSecret) {

        if (timedCache.containsKey(contactsSecret)) {
            return timedCache.get(contactsSecret);
        }


        String corpid = systemConfigProperties.getCorpid();
        String corpsecret = contactsSecret != null ? contactsSecret : systemConfigProperties.getCorpsecret();

        String getToken = SpringUtil.getProperty("qywx.getToken");

        // 获取token
        @Cleanup HttpResponse response = HttpRequest.get(getToken.replace("ID", corpid).replace("SECRET", corpsecret))
                .execute();
        if (response.getStatus() == HttpStatus.HTTP_OK) {
            String body = response.body();
            log.info("获取token结果: {}", body);
            JSONObject jsonObject = JSON.parseObject(body);
            if (jsonObject.getInteger("errcode") == 0) {
                String accessToken = jsonObject.getString("access_token");
                timedCache.put(contactsSecret, accessToken);
                return accessToken;
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
            log.info("获取用户id，参数：{}，结果: {}", reqParamJo.toJSONString(), body);
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
            log.info("获取用户信息结果: {}", body);
            JSONObject jsonObject = JSON.parseObject(body);
            if (jsonObject.getInteger("errcode") == 0) {
                return jsonObject.getString("userid");
            }
        }
        return null;
    }

    public void updateQywxUserPosition(String userid, Set<String> ptPinSet) {
        String token = getContactsToken();
        if (token != null) {
            String updateUrl = SpringUtil.getProperty("qywx.update");

            JSONObject reqParamJo = new JSONObject();
            reqParamJo.put("userid", userid);
            reqParamJo.put("position", String.join(",", ptPinSet));

            @Cleanup HttpResponse response = HttpRequest.post(updateUrl.replace("ACCESS_TOKEN", token))
                    .body(reqParamJo.toJSONString())
                    .execute();
            log.info("更新企业微信岗位信息为京东pt_pin完成，参数：{}，结果：{}", reqParamJo.toJSONString(), response.body());
        }

    }

    public void createUser(String mobile, Set<String> ptPinSet) {
        String token = getContactsToken();
        if (token != null) {
            String create = SpringUtil.getProperty("qywx.create");

            JSONObject reqParamJo = new JSONObject();
            reqParamJo.put("name", "请登录企业微信或关注微信插件修改—" + mobile.replaceAll("([0-9]{3})[0-9]{4}([0-9]{4})", "$1****$2"));
            reqParamJo.put("mobile", mobile);
            reqParamJo.put("userid", mobile);
            reqParamJo.put("biz_mail", mobile + "@" + systemConfigProperties.getDomain());
//            reqParamJo.put("email", mobile + "@" + systemConfigProperties.getDomain());
            reqParamJo.put("position", String.join(",", ptPinSet));

            @Cleanup HttpResponse response = HttpRequest.post(create.replace("ACCESS_TOKEN", token))
                    .body(reqParamJo.toJSONString())
                    .execute();
            log.info(response.body());
        }

    }


}
