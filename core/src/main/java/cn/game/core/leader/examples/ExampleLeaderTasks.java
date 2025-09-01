package cn.game.core.leader.examples;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.leader.LeaderLeaseManager.LeaderTaskContext;
import cn.game.core.leader.LeaderTask;
import cn.game.core.ops.OperationalGate;

/**
 * ExampleLeaderTasks
 *
 * 示例任务集合：展示事件驱动与批循环两种使用方式。
 */
public class ExampleLeaderTasks {

    /**
     * 事件驱动示例：通过 OperationalGate 控制是否接受触发
     */
    public static class TriggerDrivenTask implements LeaderTask {
        private static final Logger log = LoggerFactory.getLogger(TriggerDrivenTask.class);
        private final OperationalGate gate;

        public TriggerDrivenTask(OperationalGate gate) {
            this.gate = gate;
        }

        @Override
        public String name() {
            return "trigger-driven";
        }

        @Override
        public void onStart(LeaderTaskContext ctx) {
            log.info("[TriggerDrivenTask] onStart, nodeId={}", ctx.nodeId());
        }

        @Override
        public void run(LeaderTaskContext ctx) throws Exception {
            // 不再强制批循环；该任务提供一个对外方法供业务触发（示例）
            // 真实项目中，你可能把 gate 暴露给 Controller/Handler，在触发时先 gate.allowExecute()
            log.info("[TriggerDrivenTask] running in event-driven mode. nodeId={}", ctx.nodeId());
            // 演示：驻留线程等待 stop
            while (ctx.canExecute()) {
                TimeUnit.SECONDS.sleep(1);
            }
            log.info("[TriggerDrivenTask] exit run loop (not leader or lease missing).");
        }

        @Override
        public void onPause(String reason) {
            log.info("[TriggerDrivenTask] onPause, reason={}", reason);
        }

        @Override
        public void onStop(String reason) {
            log.info("[TriggerDrivenTask] onStop, reason={}", reason);
        }

        // 假设这是一个被动触发的业务入口
        public void onPlayerTriggered(String playerId) {
            if (!gate.allowExecute()) {
                log.warn("[TriggerDrivenTask] reject trigger, phase={}, reason={}", gate.currentPhase(), gate.reason());
                return;
            }
            // 执行业务逻辑
            log.info("[TriggerDrivenTask] execute for player={}, reason={}", playerId, gate.reason());
        }
    }

    /**
     * 批循环示例（保留旧风格）
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
         * 主循环：每次循环作为一个“小批次”，开头做租约健康检查（旧用法示例）
         */
        @Override
        public void run(LeaderTaskContext ctx) throws Exception {
            while (ctx.canExecute()) {
                ctx.ensureLeaseHealthy();
                long n = counter.incrementAndGet();
                log.info("[MetricsDumpTask] batch={}, nodeId={}", n, ctx.nodeId());
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
