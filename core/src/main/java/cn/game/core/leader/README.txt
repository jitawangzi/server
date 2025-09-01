Leader 执行框架使用指南

1) 初始化 ZK 与 Redis
   - 确保 ZkHelper.initIfNeeded() 在应用启动时被调用，或者在 LeaderFramework.newDefault/newWithParams 前调用。
   - RedisUtil 已通过 Apollo 加载 redisson.yaml，请保证配置正确。

2) 启动框架
   LeaderFramework framework = LeaderFramework.newDefault("/app/leader/global");
   framework.registerTask(new ExampleLeaderTasks.MetricsDumpTask());
   framework.start();

3) 任务实现建议
   public class MyTask implements LeaderTask {
       public String name() { return "my-task"; }
       public void onStart(LeaderTaskContext ctx) { ... }
       public void run(LeaderTaskContext ctx) throws Exception {
           while (ctx.canExecute()) {
               ctx.ensureLeaseHealthy();
               // 执行小批次
           }
       }
       public void onPause(String reason) { ... }
       public void onStop(String reason) { ... }
   }

4) 关键语义
   - 只有 isLeader && 持有租约 时才启动任务。
   - SUSPENDED：停止续约并暂停/停止任务，避免双主。
   - 续约：短TTL+高频续约；执行前检查剩余寿命>安全余量；连续续约失败超阈值立即停。

5) 参数建议（可在 LeaderLeaseManager 构造时调整）
   - leaseTtlSeconds: 10~20（不大于 ZK sessionTimeout）
   - renewPeriodSeconds: TTL 的 1/3~1/2
   - safetyMarginSeconds: 20%~40% TTL
   - maxRenewFailures: 2~3

6) 监控建议
   - 当前 Leader 节点ID、是否持有租约、剩余TTL
   - 续约失败计数、SUSPENDED/LOST 事件
   - 任务启动/停止原因与时长
