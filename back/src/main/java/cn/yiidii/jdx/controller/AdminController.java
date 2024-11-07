package cn.yiidii.jdx.controller;

import cn.hutool.core.util.StrUtil;
import cn.yiidii.jdx.config.prop.SystemConfigProperties;
import cn.yiidii.jdx.config.prop.SystemConfigProperties.QLConfig;
import cn.yiidii.jdx.model.R;
import cn.yiidii.jdx.model.ex.BizException;
import cn.yiidii.jdx.service.AdminService;
import cn.yiidii.jdx.service.QLService;
import cn.yiidii.jdx.util.SQLiteUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private final QLService qlService;

    @GetMapping("ql")
    public R<?> qlConfig() {
        return R.ok(adminService.getQLConfig());
    }

    @PostMapping("ql")
    public R<?> addQLConfig(@RequestBody @Validated QLConfig qlConfig) {
        List<QLConfig> qlConfigs = adminService.saveQLConfig(qlConfig);
        return R.ok(qlConfigs, "添加成功");
    }

    @DeleteMapping("ql")
    public R<?> delQLConfig(@RequestParam @NotNull(message = "displayName不能为空") String displayName) {
        List<QLConfig> qlConfigs = adminService.delQLConfig(displayName);
        return R.ok(qlConfigs, "删除成功");
    }

    @GetMapping("websiteConfig")
    public R<SystemConfigProperties.WebsiteConfig> webbsiteConfig() {
        return R.ok(systemConfigProperties.getWebsiteConfig());
    }


    @PutMapping("websiteConfig")
    public R<?> updateWebsiteConfig(@RequestBody SystemConfigProperties.WebsiteConfig websiteConfig) {
        systemConfigProperties.setWebsiteConfig(websiteConfig);
        return R.ok(null, "修改成功");
    }

    @GetMapping("qywx")
    public R<SystemConfigProperties.QywxConfig> qywxConfig() {
        return R.ok(systemConfigProperties.getQywxConfig());
    }


    @PutMapping("qywx")
    public R<SystemConfigProperties.QywxConfig> updateQywx(@RequestBody SystemConfigProperties.QywxConfig qywxConfig) {
        systemConfigProperties.setQywxConfig(qywxConfig);
        return R.ok(null, "修改成功");
    }

    @GetMapping("account")
    public R<String> account() {
        return R.ok(systemConfigProperties.getAccountConfig().getUsername());
    }


    @PutMapping("updateAccount")
    public R<?> updateAccount(@RequestBody SystemConfigProperties.AccountConfig account) {
        String username = account.getUsername();
        String password = account.getPassword();
        if (StrUtil.isBlank(username)) {
            throw new BizException("用户名不能为空");
        }
        if (StrUtil.isBlank(password)) {
            throw new BizException("密码不能为空");
        }
        if (StrUtil.isBlank(account.getOldPassword())) {
            throw new BizException("旧密码不能为空");
        }
        if (StrUtil.isNotBlank(password) &&
                StrUtil.equals(account.getOldPassword(), systemConfigProperties.getAccountConfig().getPassword())
        ) {
            systemConfigProperties.getAccountConfig().setPassword(password);
            systemConfigProperties.getAccountConfig().setUsername(username);
        }
        return R.ok(null, "修改成功");
    }

    @GetMapping(value = "updateEnv")
    public R<?> updateEnv(String mobile) {
        return R.ok(adminService.updateEnv(mobile));
    }

    @GetMapping(value = "getLatestBean")
    public R<?> getLatestBean(String time) {
        return R.ok(SQLiteUtils.getLatestBean(time));
    }

    @GetMapping(value = "getAllEnv")
    public R<?> getAllEnv() {
        List<QLService.QLAllNodeSearchResult> jdCookie = qlService.searchEnvFromAllNode("", "JD_COOKIE");
        List<List<JSONObject>> collect = jdCookie.stream().map(QLService.QLAllNodeSearchResult::getEnvs).collect(Collectors.toList());
        List<JSONObject> temp = new ArrayList<>();
        for (List<JSONObject> jsonObjects : collect) {
            temp.addAll(jsonObjects.stream().map(item -> {
                JSONObject remarks = JSONObject.parseObject(item.getString("remarks"));

                JSONObject jp = new JSONObject();

                jp.put("status", item.getInteger("status"));
                jp.put("ptPin", remarks.getString("ptPin"));
                jp.put("loginTime", remarks.getString("loginTime"));
                jp.put("wechat", remarks.getString("wechat"));
                return jp;
            }).collect(Collectors.toList()));
        }


        return R.ok(temp);
    }

    @GetMapping(value = "getAllDate")
    public R<?> getAllDate() {
        return R.ok(SQLiteUtils.getAllDate());
    }
}
