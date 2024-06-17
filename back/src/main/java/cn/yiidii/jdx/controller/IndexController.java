package cn.yiidii.jdx.controller;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.StrUtil;
import cn.yiidii.jdx.config.prop.SystemConfigProperties;
import cn.yiidii.jdx.config.prop.SystemConfigProperties.QLConfig;
import cn.yiidii.jdx.model.R;
import cn.yiidii.jdx.model.dto.JdInfo;
import cn.yiidii.jdx.model.ex.BizException;
import cn.yiidii.jdx.service.JdService;
import cn.yiidii.jdx.service.QLService;
import cn.yiidii.jdx.util.JDXUtil;
import cn.yiidii.jdx.util.jd.JDTaskUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

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
    private final SystemConfigProperties systemConfigProperties;

    @GetMapping("/jd/smsCode")
    public R<JdInfo> qrCode(@RequestParam @NotNull(message = "请填写手机号") String mobile) throws Exception {
        Assert.isTrue(PhoneUtil.isMobile(mobile), () -> {
            throw new BizException("手机号格式不正确");
        });
        JdInfo jdInfo = jdService.sendSmsCode(mobile);
        log.info(StrUtil.format("{}发送了验证码", DesensitizedUtil.mobilePhone(mobile)));
        return R.ok(jdInfo, "发送验证码成功");
    }

    @PostMapping("/jd/login")
    public R<JdInfo> login(@RequestBody JSONObject paramJo) throws Exception {
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

//        // 测试用
//        String testCookie = "pt_key=xxx7;pt_pin=jd_xxx01;";
//        String ptPin = JDXUtil.getPtPinFromCK(testCookie);
//        JdInfo jdInfo = JdInfo.builder().cookie(testCookie).ptPin(ptPin).build();

        return R.ok(jdInfo, "获取cookie成功");
    }

    @PostMapping("/ql/submitCk")
    public R<JSONObject> submitCk(@RequestBody JSONObject paramJo) throws Exception {
        String cookie = paramJo.getString("cookie");
        String mobile = paramJo.getString("mobile");
        Assert.isTrue(StrUtil.isNotBlank(cookie), () -> {
            throw new BizException("Cookie不能为空");
        });

        JSONObject result = qlService.submitCk(cookie, mobile);
        log.info(StrUtil.format("ptPin: {}提交Cookie", JDXUtil.getPtPinFromCK(cookie)));
        return R.ok(result, StrUtil.format("提交成功"));
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

    @GetMapping("cfd")
    public Object getCfdUrl(@RequestParam(required = false) String type) {
        List<QLConfig> qls = systemConfigProperties.getQls();
        for (QLConfig qlConfig : qls) {
            List<JSONObject> envs = qlService.searchEnv(qlConfig, "JD_COOKIE", 0);
            for (JSONObject env : envs) {
                String cookie = env.getString("value");
                String cfdUrl = JDTaskUtil.getCfdUrl(cookie);
                if (StrUtil.isNotBlank(cfdUrl)) {
                    if (StrUtil.equals(type, "json")) {
                        return R.ok(cfdUrl);
                    }
                    return cfdUrl;
                }
            }
        }
        throw new BizException("暂时无法获取");
    }
}
