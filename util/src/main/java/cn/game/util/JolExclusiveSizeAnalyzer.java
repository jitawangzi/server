package cn.game.util;

import org.openjdk.jol.info.ClassLayout;

import java.lang.ref.Reference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 使用反射做多根可达性标记，计算每个模块的“独占对象集合”的浅大小总和。
 * 无需 Java Agent，不使用 GraphLayout，性能稳定、无告警。
 *
 * 用法：
 *   Map<String, Object> nameToModule = ...; // 模块名 -> 模块根对象（通常是具体的 PlayerModule 实例）
 *   Map<String, JolExclusiveSizeAnalyzer.ModuleStat> stats = JolExclusiveSizeAnalyzer.analyzeExclusive(nameToModule, null);
 *
 * 注意：
 *   - 结果为浅大小之和（shallow size），不等于 retained size，但可加且稳定，适合模块间对比与趋势监控。
 *   - 请把 PLAYER_CLASS_NAME 设置为你们项目中 Player 的全限定名，以切断跨模块回边。
 */
public class JolExclusiveSizeAnalyzer {

    // 你的业务类型（仅为类型标识，可按需保留/移除）
    public static abstract class BasePlayerModule {}

    public static class ModuleStat {
        public final String name;
        public long exclusiveBytes; // 不含共享，浅大小总和
        public int exclusiveObjectCount;
        public ModuleStat(String name) { this.name = name; }
    }

    // 请替换为你们项目中 Player 的真实全限定名，确保切断回到聚合根
    private static final String PLAYER_CLASS_NAME = "cn.game.games.cache.entity.Player";

    // 类 -> 浅大小 缓存，避免重复解析
    private static final Map<Class<?>, Integer> SIZE_CACHE = new ConcurrentHashMap<>();

    // 系统类型黑名单：不计入、不展开
    private static final List<Class<?>> TYPE_BLACKLIST = List.of(
            Class.class, ClassLoader.class, Thread.class,
            java.util.concurrent.Future.class, java.util.concurrent.CompletableFuture.class,
            java.lang.reflect.Method.class, java.lang.reflect.Field.class, java.lang.reflect.Constructor.class,
            java.lang.invoke.MethodHandle.class, java.lang.invoke.MethodHandles.Lookup.class
    );

    // 字段名黑名单：切断回到聚合根或全局上下文的常见字段
    private static final Set<String> FIELD_NAME_BLACKLIST = Set.of(
            "player", "modules", "context", "manager", "cache", "service",
            "repository", "dao", "eventBus", "scheduler", "threadLocal",
            "logger", "log"
    );

    // 当对象数量异常时的安全阈值，防止个别模块极端数据拖垮分析
    private static final int DEFAULT_MAX_NODES_PER_MODULE = 500_000;

    /**
     * 对每个模块根对象进行可达性遍历，计算“独占对象集合”的浅大小之和。
     *
     * @param nameToRoot 模块名 -> 根对象
     * @param sharedSummary 可选回调，接收共享池（被多个模块可达的对象）的数量与浅大小总和。传 null 表示不需要。
     * @return 模块名 -> 统计
     */
    public static Map<String, ModuleStat> analyzeExclusive(Map<String, ?> nameToRoot,
                                                          SharedSummaryCallback sharedSummary) {
        if (nameToRoot == null || nameToRoot.isEmpty()) {
            return Collections.emptyMap();
        }

        // 固定模块顺序，保证输出稳定
        List<String> names = new ArrayList<>(nameToRoot.keySet());
        Collections.sort(names);

        // 每个根的可达集合（IdentityHashSet）以及浅大小累积
        List<IdentityHashMap<Object, Boolean>> reachSets = new ArrayList<>(names.size());
        for (int i = 0; i < names.size(); i++) reachSets.add(new IdentityHashMap<>());

        // 1) 遍历：记录可达对象身份
        for (int i = 0; i < names.size(); i++) {
            Object root = nameToRoot.get(names.get(i));
            if (root == null) continue;
            traverse(root, reachSets.get(i), DEFAULT_MAX_NODES_PER_MODULE);
        }

        // 2) 统计每个对象的“被多少根可达”
        IdentityHashMap<Object, Integer> countReach = new IdentityHashMap<>();
        for (IdentityHashMap<Object, Boolean> set : reachSets) {
            for (Object o : set.keySet()) {
                countReach.put(o, countReach.getOrDefault(o, 0) + 1);
            }
        }

        // 3) 构建每个模块的独占对象集合（countReach==1）
        Map<String, ModuleStat> result = new LinkedHashMap<>();
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            IdentityHashMap<Object, Boolean> reach = reachSets.get(i);
            ModuleStat stat = new ModuleStat(name);

            long bytes = 0L;
            int objs = 0;

            for (Object o : reach.keySet()) {
                if (countReach.getOrDefault(o, 0) == 1) {
                    bytes += shallowSizeOf(o);
                    objs++;
                }
            }
            stat.exclusiveBytes = bytes;
            stat.exclusiveObjectCount = objs;
            result.put(name, stat);
        }

        // 4) 共享池汇总（可选）
        if (sharedSummary != null) {
            long sharedBytes = 0L;
            int sharedObjs = 0;
            for (Map.Entry<Object, Integer> e : countReach.entrySet()) {
                if (e.getValue() != 1) {
                    sharedBytes += shallowSizeOf(e.getKey());
                    sharedObjs++;
                }
            }
            sharedSummary.onSharedSummary(sharedObjs, sharedBytes);
        }

        return result;
    }

    // 回调接口：共享池汇总
    public interface SharedSummaryCallback {
        void onSharedSummary(int objects, long shallowBytes);
    }

    // 浅大小（shallow size），带缓存
    private static int shallowSizeOf(Object o) {
        if (o == null) return 0;
        Class<?> c = o.getClass();
        return SIZE_CACHE.computeIfAbsent(c, k -> (int) ClassLayout.parseInstance(o).instanceSize());
    }

    // 是否系统黑名单类型（不计入、不展开）
    private static boolean isSystemBlackType(Object o) {
        if (o == null) return true;
        Class<?> c = o.getClass();
        for (Class<?> t : TYPE_BLACKLIST) {
            if (t.isAssignableFrom(c)) return true;
        }
        String n = c.getName();
        // 代理、反射、JDK 内部元数据
        if (n.startsWith("java.lang.invoke.")
                || n.startsWith("java.lang.reflect.")
                || n.startsWith("jdk.proxy.")
                || n.startsWith("sun.reflect.")
                || n.startsWith("jdk.internal.reflect.")
                || n.startsWith("java.util.logging.")) {
            return true;
        }
        return false;
    }

    // 依据类名识别 Player 聚合根；请替换为你们的真实 Player 类名
    private static boolean isPlayerAggregateObject(Object o) {
        if (o == null) return false;
        return o.getClass().getName().equals(PLAYER_CLASS_NAME)
                || o.getClass().getSimpleName().equals("Player"); // 简名匹配作为兜底
    }

    // 是否聚合字段（切断）
    private static boolean isAggregateField(Field f) {
        String name = f.getName();
        if (FIELD_NAME_BLACKLIST.contains(name)) return true;
        // 一些常见聚合命名模式
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.contains("all")
                || lower.contains("global")
                || lower.contains("registry")
                || lower.contains("pool")
                || lower.contains("container");
    }

    // 遍历：从 root 出发，记录所有可达对象到 visited 中；应用切断与黑名单
    private static void traverse(Object root, IdentityHashMap<Object, Boolean> visited, int maxNodes) {
        Deque<Object> dq = new ArrayDeque<>();
        dq.add(root);

        while (!dq.isEmpty()) {
            Object obj = dq.pollFirst();
            if (obj == null) continue;
            if (visited.put(obj, Boolean.TRUE) != null) continue;

            if (visited.size() > maxNodes) {
                // 超过安全阈值，截断本模块遍历
                return;
            }

            // 不展开系统类型
            if (isSystemBlackType(obj)) continue;
            // 切断 Player 聚合根
            if (isPlayerAggregateObject(obj)) continue;

            Class<?> c = obj.getClass();

            // 引用类型包装（WeakReference/SoftReference/PhantomReference）
            if (obj instanceof Reference<?> ref) {
                Object t = ref.get();
                if (t != null) dq.add(t);
                continue;
            }

            // Map：枚举 key 和 value
            if (obj instanceof Map<?, ?> m) {
                for (Object k : m.keySet()) if (k != null) dq.add(k);
                for (Object v : m.values()) if (v != null) dq.add(v);
                // 不反射展开 Map 内部结构，稳定且更高效
            }
            // Collection/Iterable：枚举元素
            else if (obj instanceof Collection<?> coll) {
                for (Object e : coll) if (e != null) dq.add(e);
            } else if (obj instanceof Iterable<?> it) {
                for (Object e : it) if (e != null) dq.add(e);
            }

            // 数组：对象元素
            if (c.isArray()) {
                if (!c.getComponentType().isPrimitive()) {
                    int len = Array.getLength(obj);
                    for (int i = 0; i < len; i++) {
                        Object e = Array.get(obj, i);
                        if (e != null) dq.add(e);
                    }
                }
                continue;
            }

            // 反射字段：非静态、非原始类型，应用切断
            Class<?> cur = c;
            while (cur != null) {
                Field[] fs = cur.getDeclaredFields();
                for (Field f : fs) {
                    int mod = f.getModifiers();
                    if (Modifier.isStatic(mod)) continue;
                    if (f.getType().isPrimitive()) continue;
                    if (isAggregateField(f)) continue;

                    try {
                        if (!f.canAccess(obj)) f.setAccessible(true);
                        Object ref = f.get(obj);
                        if (ref == null) continue;
                        if (isSystemBlackType(ref)) continue;
                        if (isPlayerAggregateObject(ref)) continue;
                        dq.add(ref);
                    } catch (InaccessibleObjectException e) {
                        // 可按需统计次数，必要时在 JVM 启动参数加入：
                        // --add-opens java.base/java.util=ALL-UNNAMED
                    } catch (IllegalAccessException ignored) {
                    } catch (Throwable ignored) {
                    }
                }
                cur = cur.getSuperclass();
            }
        }
    }
}