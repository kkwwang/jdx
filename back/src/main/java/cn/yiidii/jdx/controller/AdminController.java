package cn.yiidii.jdx.controller;

import cn.hutool.core.util.StrUtil;
import cn.yiidii.jdx.config.prop.SystemConfigProperties;
import cn.yiidii.jdx.config.prop.SystemConfigProperties.QLConfig;
import cn.yiidii.jdx.model.R;
import cn.yiidii.jdx.model.ex.BizException;
import cn.yiidii.jdx.service.AdminService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author ed w
 * @since 1.0
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final SystemConfigProperties systemConfigProperties;

    @GetMapping("ql")
    public R<?> qlConfig() {
        JSONArray qlConfig = adminService.getQLConfig();
        return R.ok(qlConfig);
    }

    @PostMapping("ql")
    public R<?> addQLConfig(@RequestBody @Validated QLConfig qlConfig) {
        List<QLConfig> qlConfigs = adminService.addQLConfig(qlConfig);
        return R.ok(qlConfigs, "添加成功");
    }

    @PutMapping("ql")
    public R<?> updateQLConfig(@RequestBody @Validated QLConfig qlConfig) {
        List<QLConfig> qlConfigs = adminService.updateQLConfig(qlConfig);
        return R.ok(qlConfigs, "修改成功");
    }

    @DeleteMapping("ql")
    public R<?> delQLConfig(@RequestParam @NotNull(message = "displayName不能为空") String displayName) {
        List<QLConfig> qlConfigs = adminService.delQLConfig(displayName);
        return R.ok(qlConfigs, "删除成功");
    }

    @GetMapping("config")
    public R<?> getConfig() {
        JSONObject result = new JSONObject();
        result.put("title", systemConfigProperties.getTitle());
        result.put("notice", systemConfigProperties.getNotice());
        result.put("bottomNotice", systemConfigProperties.getIndexBottomNotice());
        result.put("username", systemConfigProperties.getUsername());
        result.put("password", systemConfigProperties.getPassword());
        result.put("corpid", systemConfigProperties.getCorpid());
        result.put("corpsecret", systemConfigProperties.getCorpsecret());
        result.put("agentid", systemConfigProperties.getAgentid());
        result.put("qywxKey", systemConfigProperties.getQywxKey());
        return R.ok(result);
    }


    @PutMapping("websiteConfig")
    public R<?> updateWebsiteConfig(@RequestBody JSONObject paramJo) {
        JSONObject websiteConfig = adminService.updateWebsiteConfig(paramJo);
        return R.ok(websiteConfig, "修改成功");
    }


    @PutMapping("qywx")
    public R<?> updateQywx(@RequestBody JSONObject paramJo) {
        systemConfigProperties.setQywxKey(paramJo.getString("qywxKey"));
        return R.ok(paramJo, "修改成功");
    }


    @PutMapping("updateAccount")
    public R<?> updateAccount(@RequestBody JSONObject paramJo) {
        String username = paramJo.getString("username");
        String password = paramJo.getString("password");
        if (StrUtil.isBlank(username)) {
            throw new BizException("用户名不能为空");
        }
        if (StrUtil.isBlank(password)) {
            throw new BizException("密码不能为空");
        }
        systemConfigProperties.setUsername(username);
        systemConfigProperties.setPassword(password);
        return R.ok(null, "修改成功");
    }
}
