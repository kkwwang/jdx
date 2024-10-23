package cn.yiidii.jdx.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWTUtil;
import cn.yiidii.jdx.config.prop.SystemConfigProperties;
import cn.yiidii.jdx.model.R;
import cn.yiidii.jdx.model.ex.BizException;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * auth
 *
 * @author ed w
 * @since 1.0
 */
@Slf4j
@Validated
@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {

    private final SystemConfigProperties systemConfigProperties;

    /**
     * 社交登录81726148
     *
     * @param paramJo paramJo
     * @return R
     */
    @PostMapping("/login")
    public R<?> login(@RequestBody JSONObject paramJo) throws Exception {
        String username = paramJo.getString("username");
        String password = paramJo.getString("password");

        Assert.isTrue(StrUtil.isNotBlank(username), () -> {
            throw new BizException("用户名不能为空 ");
        });
        Assert.isTrue(StrUtil.isNotBlank(password), () -> {
            throw new BizException("密码不能为空");
        });
        if (!StrUtil.equals(systemConfigProperties.getUsername(), username)
                || !StrUtil.equals(systemConfigProperties.getPassword(), password)) {
            throw new BizException("用户名或密码不正确");
        }
        JSONObject info = new JSONObject();
        info.put("username", username);
        info.put("exp", DateUtil.offsetHour(new Date(), 24));
        info.put("token", JWTUtil.createToken(info, "jdx".getBytes(StandardCharsets.UTF_8)));

        info.remove("exp");

        log.info(StrUtil.format("登录结果: {}", info.toJSONString()));
        return R.ok(info, "登陆成功");
    }

}
