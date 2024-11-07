package cn.game.core.task;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

/**    
 * 这个类只用来EnableScheduling
 * 2024年11月7日 18:07:16
 * @author SYQ
 */
@Component
@EnableScheduling
public class SchedulerConfig {

//    @Bean
//    public ThreadPoolTaskScheduler taskScheduler() {
//        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
//		scheduler.setPoolSize(5); // 设置线程池大小
//		scheduler.setThreadNamePrefix("scheduled-task-"); // 设置线程名称前缀
//        return scheduler;
//    }

}