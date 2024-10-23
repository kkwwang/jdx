package cn.yiidii.jdx.controller;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.StrUtil;
import cn.yiidii.jdx.config.prop.SystemConfigProperties;
import cn.yiidii.jdx.model.R;
import cn.yiidii.jdx.model.dto.JdInfo;
import cn.yiidii.jdx.model.ex.BizException;
import cn.yiidii.jdx.service.JdService;
import cn.yiidii.jdx.service.QLService;
import cn.yiidii.jdx.util.CheckUtil;
import cn.yiidii.jdx.util.JDXUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * JdController
 *
 * @author ed w
 * @since 1.0
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class IndexController {

    private final JdService jdService;
    private final QLService qlService;
    private final CheckUtil checkUtil;
    private final SystemConfigProperties systemConfigProperties;

    @GetMapping("/jd/smsCode")
    public R<JdInfo> qrCode(@RequestParam @NotNull(message = "请填写手机号") String mobile) throws Exception {
        Assert.isTrue(PhoneUtil.isMobile(mobile), () -> {
            throw new BizException("手机号格式不正确");
        });

        Set<String> bindQywx = checkUtil.checkBindQywx(mobile);
        if (bindQywx.isEmpty()) {
            return R.failed("该手机号未绑定企业微信，请先扫码关注下方插件，若加入失败，请尝试下载企业微信绑定手机号或联系管理员!!!");
        }

        if (checkUtil.envIsEnable(mobile)) {
            return R.failed("cookie还在有效期，请勿重复登录");
        }

        JdInfo jdInfo = jdService.sendSmsCode(mobile);
        jdInfo.setQywxUserId(bindQywx);
        log.info(StrUtil.format("{}发送了验证码", DesensitizedUtil.mobilePhone(mobile)));
        return R.ok(jdInfo, "发送验证码成功");
    }

    @PostMapping("/jd/login")
    public R<JSONObject> login(@RequestBody JSONObject paramJo) throws Exception {
        String mobile = paramJo.getString("mobile");
        String code = paramJo.getString("code");
        Assert.isTrue(StrUtil.isNotBlank(mobile), () -> {
            throw new BizException("手机号不能为空");
        });
        Assert.isTrue(PhoneUtil.isMobile(mobile), () -> {
            throw new BizException("手机号格式不正确");
        });
        Assert.isTrue(StrUtil.isNotBlank(code), () -> {
            throw new BizException("验证码不能为空");
        });

        JdInfo jdInfo = jdService.login(mobile, code);
        log.info(StrUtil.format("{}获取了京东Cookie", DesensitizedUtil.mobilePhone(mobile)));


        JSONObject result = qlService.submitCk(jdInfo.getCookie(), mobile);
        log.info(StrUtil.format("ptPin: {}提交Cookie", JDXUtil.getPtPinFromCK(jdInfo.getCookie())));

        return R.ok(result, "登录成功");
    }


    @GetMapping("info")
    public R<?> getBaseInfo() {
        JSONObject jo = new JSONObject();
        jo.put("title", systemConfigProperties.getTitle());
        jo.put("notice", systemConfigProperties.getNotice());
        jo.put("bottomNotice", systemConfigProperties.getIndexBottomNotice());
        jo.put("remain", systemConfigProperties.getQls().stream()
                .filter(ql -> ql.getDisabled() == 0 && ql.getUsed() < ql.getMax())
                .map(ql -> ql.getMax() - ql.getUsed())
                .reduce(0, (a, b) -> a + b));
        return R.ok(jo);
    }

}
