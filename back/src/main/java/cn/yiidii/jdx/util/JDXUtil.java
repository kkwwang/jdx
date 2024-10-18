package cn.yiidii.jdx.util;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.yiidii.jdx.model.ex.BizException;
import lombok.experimental.UtilityClass;

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


}
