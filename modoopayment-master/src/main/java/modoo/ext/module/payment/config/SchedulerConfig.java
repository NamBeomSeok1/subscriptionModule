package modoo.ext.module.payment.config;

import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

// @Configuration
public class SchedulerConfig implements SchedulingConfigurer {
    private final int SCHEDULER_POOL_SIZE = 1; //결제모듈이 중복실행되면 안된다.

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();

        threadPoolTaskScheduler.setPoolSize(SCHEDULER_POOL_SIZE);
        threadPoolTaskScheduler.setThreadNamePrefix("modoopay-scheduled-task-pool-");
        threadPoolTaskScheduler.initialize();

        scheduledTaskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
    }
}