package cn.yiidii.jdx.controller;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.yiidii.jdx.config.prop.SystemConfigProperties;
import cn.yiidii.jdx.model.R;
import cn.yiidii.jdx.model.dto.JdInfo;
import cn.yiidii.jdx.model.ex.BizException;
import cn.yiidii.jdx.service.AdminService;
import cn.yiidii.jdx.service.JdService;
import cn.yiidii.jdx.service.QLService;
import cn.yiidii.jdx.util.JDXUtil;
import cn.yiidii.jdx.util.QywxUtil;
import cn.yiidii.jdx.util.SQLiteUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
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
    private final QywxUtil qywxUtil;
    private final SystemConfigProperties systemConfigProperties;
    private final AdminService adminService;

    @GetMapping("/jd/smsCode")
    public R<JdInfo> smsCode(@RequestParam @NotNull(message = "请填写手机号") String mobile) throws Exception {
        Assert.isTrue(PhoneUtil.isMobile(mobile), () -> {
            throw new BizException("手机号格式不正确");
        });


        if (qywxUtil.envIsEnable(mobile)) {
            return R.failed("cookie还在有效期，请勿重复登录");
        }

        JdInfo jdInfo = jdService.sendSmsCode(mobile);

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

        if (qywxUtil.envIsEnable(mobile)) {
            return R.failed("cookie还在有效期，请勿重复登录");
        }

        Assert.isTrue(StrUtil.isNotBlank(code), () -> {
            throw new BizException("验证码不能为空");
        });

        JdInfo jdInfo = jdService.login(mobile, code);
        log.info(StrUtil.format("{}获取了京东Cookie", DesensitizedUtil.mobilePhone(mobile)));

        Set<String> bindQywx = qywxUtil.checkBindQywx(mobile);
        if (bindQywx.isEmpty()) {
            jdInfo.setQywxUserId(new HashSet<>(Collections.singletonList(mobile)));
            qywxUtil.createUser(mobile, new HashSet<>(Collections.singletonList(jdInfo.getPtPin())));
        } else {
            jdInfo.setQywxUserId(bindQywx);
        }

        JSONObject result = qlService.submitCk(jdInfo.getCookie(), mobile);
        log.info(StrUtil.format("ptPin: {}提交Cookie", JDXUtil.getPtPinFromCK(jdInfo.getCookie())));
        // 异步联动修正数据
        adminService.updateEnv(mobile);

        // 记录登录信息
        SQLiteUtils.insertLoginLog(mobile, jdInfo.getPtPin(), new Date());


        return R.ok(result, "登录成功");
    }


    @GetMapping("info")
    public R<?> getBaseInfo() {
        JSONObject jo = new JSONObject();
        jo.put("title", systemConfigProperties.getWebsiteConfig().getTitle());
        jo.put("notice", systemConfigProperties.getWebsiteConfig().getNotice());
        jo.put("version", SpringUtil.getProperty("spring.application.version"));
        jo.put("bottomNotice", systemConfigProperties.getWebsiteConfig().getIndexBottomNotice());
        jo.put("remain", systemConfigProperties.getQls().stream()
                .filter(ql -> ql.getDisabled() == 0 && ql.getUsed() < ql.getMax())
                .map(ql -> ql.getMax() - ql.getUsed())
                .reduce(0, Integer::sum));
        return R.ok(jo);
    }

    @GetMapping("/jd/getBean")
    public R<JSONArray> getBean(String mobile) {
        return R.ok(SQLiteUtils.getBeanByMobile(mobile));
    }

    @GetMapping("/jd/getLoginLog")
    public R<JSONArray> getLoginLog(String mobile) {
        return R.ok(SQLiteUtils.getLoginLog(mobile));
    }
}
