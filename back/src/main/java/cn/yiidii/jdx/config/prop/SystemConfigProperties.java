package cn.yiidii.jdx.config.prop;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.yiidii.jdx.support.ITask;
import cn.yiidii.jdx.util.ScheduleTaskUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 系统配置
 *
 * @author ed w
 * @since 1.0
 */
@Data
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemConfigProperties implements InitializingBean, ITask {

    public static final String SYSTEM_CONFIG_FILE_PAH = System.getProperty("user.dir") + File.separator + "config" + File.separator + "config.json";
    private static boolean INIT = false;

    @JSONField(serialize = false, deserialize = false)
    private final ScheduleTaskUtil scheduleTaskUtil;

    private String username = "admin";
    private String password = "123465";

    private String title;
    private String notice = "这是一条测试公告，只支持顶部";
    private String indexBottomNotice = "这是底部说明，只支持html";
    //    private String checkCookieCron = "0 0 12 * * ?";
    private List<QLConfig> qls;

    private String qywxKey = null;
    private String adminQywxId = null;

    private String corpid = "";
    private String corpsecret = "";
    private String domain = "";
    private String contactsSecret = "";
    private String agentid = "";

    @PostConstruct
    public void init() {
        INIT = true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 根据config.json赋值
        update(true);
    }

    public SystemConfigProperties update(boolean throwException) {
        try {
            String configStr = FileUtil.readUtf8String(SYSTEM_CONFIG_FILE_PAH);
            JSONObject configJo = JSONObject.parseObject(configStr);
            BeanUtil.copyProperties(configJo, this);
            return this;
        } catch (Exception e) {
            if (throwException) {
                throw new IllegalArgumentException(StrUtil.format("{}不存在", SYSTEM_CONFIG_FILE_PAH));
            }
            log.error("更新配置文件[{}]发生异常, e: {}", SYSTEM_CONFIG_FILE_PAH, e.getMessage());
            return null;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QLConfig {

        @NotNull(message = "displayName不能为空")
        private String displayName;
        @NotNull(message = "url不能为空")
        private String url;
        @NotNull(message = "clientId不能为空")
        private String clientId;
        @NotNull(message = "clientSecret不能为空")
        private String clientSecret;

        private Integer used = 0;
        @NotNull(message = "最大数量不能为空")
        private Integer max = 30;
        @NotNull(message = "是否禁用必选")
        private Integer disabled = 0;
    }

    public QLConfig getQLConfigByDisplayName(String displayName) {
        return qls.stream().filter(ql -> StrUtil.equalsIgnoreCase(displayName, ql.getDisplayName()))
                .findFirst().orElse(null);
    }

    @Override
    public void startTimerTask() {
        scheduleTaskUtil.startCron("QL_timerPersistSystemConfig", () -> this.timerPersistSystemConfig(), "0/30 * * * * ?");
    }

    private void timerPersistSystemConfig() {
        if (!INIT) {
            return;
        }
        Thread.currentThread().setName(String.format(Thread.currentThread().getName(), "SYS_timerPersistSystemConfig"));
        String prettyJa = JSONUtil.toJsonPrettyStr(JSONObject.toJSONString(this));
        FileUtil.writeString(prettyJa, SYSTEM_CONFIG_FILE_PAH, StandardCharsets.UTF_8);
    }
}
