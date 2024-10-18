package cn.yiidii.jdx.util;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;

import java.util.Objects;

/**
 * IdUtil
 *
 * @author ed w
 * @since 1.0
 */
@UtilityClass
public class IdUtil {

    private static final Snowflake SNOWFLAKE = new Snowflake(0);

    public String randomSnowflakeId(String prefix) {
        return StrUtil.format("{}{}{}", Objects.isNull(prefix) ? "" : prefix, StrUtil.isNotBlank(prefix) ? "_" : "", SNOWFLAKE.nextIdStr());
    }

}
