package cn.yiidii.jdx.util;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.yiidii.jdx.model.ex.BizException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * JDXUtil
 *
 * @author ed w
 * @since 1.0
 */
@UtilityClass
public class JDXUtil {

    public String getPtPinFromCK(String cookie) {
        cookie = StrUtil.isBlank(cookie) ? "" : ReUtil.replaceAll(cookie, "\\s+", "");
        try {
            return Arrays.stream(cookie.split(";"))
                    .filter(e -> e.contains("pt_pin"))
                    .findFirst().orElse("")
                    .split("=")[1];
        } catch (Exception e) {
            throw new BizException("Cookie格式不正确");
        }
    }

    public String getNiceName(String cookie) {
        @Cleanup HttpResponse userInfoResponse = HttpRequest.get("https://me-api.jd.com/user_new/info/GetJDUserInfoUnion")
                .cookie(cookie)
                .execute();

        if (userInfoResponse.getStatus() == HttpStatus.HTTP_OK) {
            String body = userInfoResponse.body();
            JSONObject jsonObject = JSON.parseObject(body);
            if (jsonObject.getIntValue("retcode") == 0) {
                JSONObject data = jsonObject.getJSONObject("data");
                if (null != data && !data.isEmpty()) {
                    JSONObject userInfo = data.getJSONObject("userInfo");
                    if (null != userInfo && !userInfo.isEmpty()) {
                        // 昵称
                        JSONObject baseInfo = userInfo.getJSONObject("baseInfo");
                        if (null != baseInfo) {
                            String nickname = baseInfo.getString("nickname");
                            if (StringUtils.hasText(nickname)) {
                                return nickname;
                            }
                        }
                    }
                }
            }
        }
        return null;

    }


}
