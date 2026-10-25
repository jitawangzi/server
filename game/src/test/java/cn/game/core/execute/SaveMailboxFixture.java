package cn.game.core.execute;

import java.util.concurrent.Executors;
import cn.game.util.DateUtil;

/** 创建不启动全局监控/定时器的真实邮箱服务，用于验证玩家等待数据库完成时无自锁。 */
public final class SaveMailboxFixture {
    /** 工具类不实例化。 */
    private SaveMailboxFixture() {}
    /** @return 调用方负责关闭的真实执行服务 */
    public static TaskExecutorService create() {
        return new TaskExecutorService(TaskExecutionConfig.builder().defaultTaskTimeoutMs(0).build(),
                new OneToOneMailboxManager(8), Executors.newVirtualThreadPerTaskExecutor(),
                (action, delay, unit) -> { throw new IllegalStateException("本测试不安排任务重试"); }, DateUtil::currentTimeMillis);
    }
}
