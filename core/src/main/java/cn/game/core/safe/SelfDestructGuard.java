package cn.game.core.safe;

import cn.game.util.ZkHelper;
import cn.game.util.RedisUtil;
import org.apache.curator.framework.state.ConnectionState;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 自毁保护：复用现有的 ZkHelper.curator 和 RedisUtil.getRedis()
 */
public class SelfDestructGuard {

  // 配置
  public static class Config {
    public Duration checkInterval = Duration.ofSeconds(5);     // 健康检查频率
    public Duration protectWindow = Duration.ofMinutes(10);    // 触发窗口：连续坏状态 N 分钟
    public Duration redisWindow = Duration.ofSeconds(30);      // Redis 失败率滑窗
    public double redisFailRateThreshold = 0.5;                // Redis 失败率阈值
    public int exitCode = 42;                                  // 自毁退出码
    public boolean logDetails = true;                          // 打印细节
  }

  // 写网关：所有写必须走这里，便于一键停写
  public static class WriterGate {
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final ExecutorService single = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "writer-gate");
      t.setDaemon(true);
      return t;
    });

    public boolean isPaused() { return paused.get(); }
    public void pause() { paused.set(true); }
    public void resume() { paused.set(false); }

    // 将你的写操作包成 Callable 或 Runnable，走这个入口
    public <T> Future<T> execute(Callable<T> task) {
      if (paused.get()) {
        CompletableFuture<T> cf = new CompletableFuture<>();
        cf.completeExceptionally(new IllegalStateException("Writes paused by protect mode"));
        return cf;
      }
      return single.submit(() -> {
        if (paused.get()) throw new IllegalStateException("Writes paused by protect mode");
        return task.call();
      });
    }
    public Future<?> execute(Runnable r) {
      return execute(Executors.callable(r, null));
    }

    public void shutdown() {
      single.shutdownNow();
    }
  }

  // Redis 滑动窗口失败率监控（复用现有 RedissonClient）
  static class RedisMonitor {
    private final RedissonClient redisson;
    private final Duration window;
    private final ConcurrentLinkedQueue<Long> stamps = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Boolean> oks = new ConcurrentLinkedQueue<>();

    RedisMonitor(RedissonClient redisson, Duration window) {
      this.redisson = redisson;
      this.window = window;
    }

    // 轻量健康探针：用 getKeys().count() 或 ping 类操作
    void tick() {
      boolean ok = false;
      try {
        // 轻操作：统计 key 数会访问集群，足够检测连通；也可换成读一个固定小key
        redisson.getKeys().count(); 
        ok = true;
      } catch (Throwable t) {
        ok = false;
      }
      long now = System.currentTimeMillis();
      stamps.add(now);
      oks.add(ok);
      // 清理过窗数据
      long cutoff = now - window.toMillis();
      while (true) {
        Long headTs = stamps.peek();
        if (headTs == null || headTs >= cutoff) break;
        stamps.poll();
        oks.poll();
      }
    }

    double failRate() {
      int total = 0, fail = 0;
      for (Boolean b : oks) {
        total++;
        if (b == null || !b) fail++;
      }
      if (total == 0) return 0.0;
      return (double) fail / total;
    }
  }

  // 保护器主体
  public static class HealthGuard {
    private final Config cfg;
    private final WriterGate gate;
    private final RedisMonitor redisMonitor;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "health-guard");
      t.setDaemon(true);
      return t;
    });

    private volatile Instant protectSince = null;

    public HealthGuard(Config cfg, WriterGate gate) {
      this.cfg = cfg;
      this.gate = gate;
      this.redisMonitor = new RedisMonitor(RedisUtil.getRedis(), cfg.redisWindow);
    }

    public void start() {
      scheduler.scheduleAtFixedRate(this::tick, 0, cfg.checkInterval.toMillis(), TimeUnit.MILLISECONDS);
    }

    public void stop() {
      scheduler.shutdownNow();
    }

    private void tick() {
      // 1) 刷新探针
      redisMonitor.tick();
      boolean zkConnected = isZkConnected();
      boolean redisBad = redisMonitor.failRate() >= cfg.redisFailRateThreshold;

      // 2) 决策：ZK 不连通 且 Redis 失败率高 => 进入/维持保护
      if (!zkConnected && redisBad) {
        if (protectSince == null) {
          if (!gate.isPaused()) {
            gate.pause(); // 立即停写
            log("ENTER_PROTECT", "zk=" + zkState() + ", redisFailRate=" + fmt(redisMonitor.failRate()));
          }
          protectSince = Instant.now();
        } else {
          Duration el = Duration.between(protectSince, Instant.now());
          if (el.compareTo(cfg.protectWindow) >= 0) {
            log("SELF_DESTRUCT", "elapsed=" + el.toMinutes() + "m, zk=" + zkState() +
                ", redisFailRate=" + fmt(redisMonitor.failRate()));
            // 一致性优先：不主动释放长TTL锁，直接退出由编排系统拉起或保持下线
            System.exit(cfg.exitCode);
          }
        }
      } else {
        // 条件不足，尝试退出保护（需要两侧都健康）
        if (protectSince != null) {
          if (zkConnected && !redisBad) {
            gate.resume();
            log("EXIT_PROTECT", "zk=" + zkState() + ", redisFailRate=" + fmt(redisMonitor.failRate()));
            protectSince = null;
          } else {
            // 仍维持暂停，但不计时自毁
            log("STAY_PAUSED", "zk=" + zkState() + ", redisFailRate=" + fmt(redisMonitor.failRate()));
          }
        }
      }
    }

    private boolean isZkConnected() {
      // Curator 会在 state listener 中维护状态；这里简化为通过 internalState 查看最近状态
      ConnectionState s = ZkHelper.curator.getConnectionStateListenable() == null
          ? null : ZkHelper.curator.getZookeeperClient().isConnected() ? ConnectionState.CONNECTED : null;
      // 上面一行只是快速判断；更稳妥的是自己注册一个 listener 维护最近状态：
      // 见下方 registerStateListener()
      return currentZkConnected.get();
    }

    private String zkState() {
      return currentZkState.get();
    }

    private String fmt(double v) {
      return String.format("%.2f", v);
    }

    // ------- ZK 状态监听（建议在 start 之前调用一次） -------
    private final AtomicBoolean currentZkConnected = new AtomicBoolean(false);
    private final AtomicReference<String> currentZkState = new AtomicReference<>("UNKNOWN");

    public void registerStateListener() {
      ZkHelper.curator.getConnectionStateListenable().addListener((c, newState) -> {
        currentZkState.set(newState.name());
        switch (newState) {
          case CONNECTED:
          case RECONNECTED:
            currentZkConnected.set(true); break;
          default:
            currentZkConnected.set(false);
        }
        if (cfg.logDetails) {
          log("ZK_STATE", newState.name());
        }
      });
      // 初始快照
      currentZkConnected.set(ZkHelper.curator.getZookeeperClient().isConnected());
      currentZkState.set(currentZkConnected.get() ? "CONNECTED" : "SUSPENDED");
    }

    private void log(String event, String msg) {
      if (!cfg.logDetails) return;
      System.out.println(Instant.now() + " [" + event + "] " + msg);
    }
  }

  // 便捷组装：应用启动时调用
  public static class Bootstrap {
    private final WriterGate writerGate = new WriterGate();
    private final HealthGuard guard;

    public Bootstrap(Config cfg) {
      // 确保你的 ZkHelper/RedisUtil 已完成静态初始化
      RedissonClient redisson = RedisUtil.getRedis();
      if (redisson == null) throw new IllegalStateException("Redisson not initialized");
      if (ZkHelper.curator == null) throw new IllegalStateException("Curator not initialized");

      this.guard = new HealthGuard(cfg, writerGate);
      this.guard.registerStateListener();
    }

    public WriterGate writerGate() { return writerGate; }
    public void start() { guard.start(); }
    public void stop() {
      try { guard.stop(); } catch (Exception ignore) {}
      try { writerGate.shutdown(); } catch (Exception ignore) {}
    }
  }
}