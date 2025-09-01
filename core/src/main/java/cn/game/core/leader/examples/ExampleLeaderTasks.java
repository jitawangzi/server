package cn.game.core.leader.examples;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.leader.LeaderLeaseManager.LeaderTaskContext;
import cn.game.core.leader.LeaderTask;

/**
 * ExampleLeaderTasks
 *
 * <p>示例任务集合：演示如何实现 LeaderTask 并在批间进行租约健康检查。</p>
 */
public class ExampleLeaderTasks {

    /**
     * 示例任务：周期打印计数
     */
    public static class MetricsDumpTask implements LeaderTask {
        private static final Logger log = LoggerFactory.getLogger(MetricsDumpTask.class);
        private final AtomicLong counter = new AtomicLong();

        @Override
        public String name() {
            return "metrics-dump";
        }

        @Override
        public void onStart(LeaderTaskContext ctx) {
            log.info("[MetricsDumpTask] onStart, nodeId={}", ctx.nodeId());
        }

        /**
         * 主循环：每次循环作为一个“小批次”，开头做租约健康检查
         */
        @Override
        public void run(LeaderTaskContext ctx) throws Exception {
            while (ctx.canExecute()) {
                // 批前确保具备执行权且租约健康
                ctx.ensureLeaseHealthy();

                long n = counter.incrementAndGet();
                log.info("[MetricsDumpTask] batch={}, nodeId={}", n, ctx.nodeId());

                // 模拟批次处理耗时
                TimeUnit.SECONDS.sleep(2);
            }
            log.info("[MetricsDumpTask] exit run loop (not leader or lease missing).");
        }

        @Override
        public void onPause(String reason) {
            log.info("[MetricsDumpTask] onPause, reason={}", reason);
        }

        @Override
        public void onStop(String reason) {
            log.info("[MetricsDumpTask] onStop, reason={}", reason);
        }
    }
}
