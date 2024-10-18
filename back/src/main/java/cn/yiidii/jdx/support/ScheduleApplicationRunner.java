package cn.yiidii.jdx.support;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Map;


/**
 * 定时任务
 *
 * @author ed w
 * @since 1.0
 */
@Slf4j
@Component
public class ScheduleApplicationRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 启动定时任务
        Map<String, ITask> beansOfType = SpringUtil.getApplicationContext().getBeansOfType(ITask.class);
        beansOfType.values().forEach(ITask::startTimerTask);
    }
}
