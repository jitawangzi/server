package cn.game.games.net.game.gm.acceptance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.ActiveServerListManager;
import cn.game.core.base.ServerContext;
import cn.game.core.exception.LogicException;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.gm.GmSecurityPolicy;
import cn.game.games.net.game.gm.acceptance.suites.CrossGuildWarModuleAcceptanceSuite;
import cn.game.games.net.game.gm.acceptance.suites.CrossWorldBossModuleAcceptanceSuite;
import cn.game.games.net.game.gm.acceptance.suites.GuildModuleAcceptanceSuite;
import cn.game.games.net.game.gm.acceptance.suites.ItemModuleAcceptanceSuite;
import cn.game.games.net.game.gm.acceptance.suites.ShopModuleAcceptanceSuite;
import cn.game.games.net.game.helper.PlayerSaveCoordinator;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.util.ServerType;
import cn.game.util.SpringContextLoader;

/**
 * 进程内模块自动化双轨验收引擎。
 * 负责套件生命周期注册、生产环境硬阻断、真实服务器集群拓扑校验、沙盒玩家上下文调度与多态配表矩阵扫描编排。
 *
 * @author shuyongqiang
 */
public class ModuleAcceptanceEngine {

    private static final Logger log = LoggerFactory.getLogger(ModuleAcceptanceEngine.class);
    private static final ModuleAcceptanceEngine INSTANCE = new ModuleAcceptanceEngine();

    public static ModuleAcceptanceEngine getInstance() {
        return INSTANCE;
    }

    private final Map<Integer, ModuleAcceptanceSuite> suites = new ConcurrentHashMap<>();

    private ModuleAcceptanceEngine() {
        // 自动注册内置模块验收套件
        registerSuite(new ItemModuleAcceptanceSuite());
        registerSuite(new ShopModuleAcceptanceSuite());
        registerSuite(new GuildModuleAcceptanceSuite());
        registerSuite(new CrossWorldBossModuleAcceptanceSuite());
        registerSuite(new CrossGuildWarModuleAcceptanceSuite());
    }

    /**
     * 判断当前是否处于真实启动的游戏服务器容器环境（非离线JUnit单测替身）
     *
     * @return true表示Spring容器、Processor与Vert.x实例均已真实就绪
     */
    public boolean isLiveServerRuntime() {
        try {
            return SpringContextLoader.getContext() != null
                    && ServerContext.getInstance().getProcessor() != null
                    && VxHolder.vertx != null;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * 检查指定套件所需的服务器集群节点拓扑状态
     *
     * @param suite 验收套件
     * @return 集群拓扑诊断详情字典
     */
    public Map<String, Object> inspectClusterTopology(ModuleAcceptanceSuite suite) {
        Map<String, Object> topology = new LinkedHashMap<>();
        boolean live = isLiveServerRuntime();
        topology.put("liveServerRuntime", live);
        String currentServerId = ServerContext.getInstance().getServerId();
        ServerType currentType = ServerContext.getInstance().getServerType();
        topology.put("currentServerId", currentServerId != null ? currentServerId : "");
        topology.put("currentServerType", currentType != null ? currentType.name() : (live ? ServerType.Game.name() : "OFFLINE_SANDBOX"));

        List<ServerType> reqTypes = (suite != null && suite.getRequiredServerTypes() != null && !suite.getRequiredServerTypes().isEmpty())
                ? suite.getRequiredServerTypes()
                : List.of(ServerType.Game);
        List<String> requiredNames = new ArrayList<>();
        List<String> missingTypes = new ArrayList<>();
        Map<String, List<String>> activeNodes = new LinkedHashMap<>();

        for (ServerType st : ServerType.values()) {
            try {
                Set<String> set = ActiveServerListManager.getInstance().getServerSet(st);
                if (set != null && !set.isEmpty()) {
                    activeNodes.put(st.name(), new ArrayList<>(set));
                }
            } catch (Throwable ignored) {
            }
        }
        if (live && currentServerId != null && !currentServerId.isBlank()) {
            String typeKey = currentType != null ? currentType.name() : ServerType.Game.name();
            List<String> list = activeNodes.computeIfAbsent(typeKey, k -> new ArrayList<>());
            if (!list.contains(currentServerId)) {
                list.add(currentServerId);
            }
        }
        for (ServerType req : reqTypes) {
            if (req == null) {
                continue;
            }
            requiredNames.add(req.name());
            List<String> nodes = activeNodes.get(req.name());
            if (!live || nodes == null || nodes.isEmpty()) {
                missingTypes.add(req.name());
            }
        }
        topology.put("requiredServerTypes", requiredNames);
        topology.put("activeServers", activeNodes);
        topology.put("missingServerTypes", missingTypes);
        topology.put("ready", live && missingTypes.isEmpty());
        return topology;
    }

    /**
     * 注册模块验收套件
     *
     * @param suite 验收套件实例
     */
    public void registerSuite(ModuleAcceptanceSuite suite) {
        if (suite != null) {
            suites.put(suite.getModuleId(), suite);
            log.info("注册模块自动化验收套件: moduleId=0x{}, key={}, title={}",
                    Integer.toHexString(suite.getModuleId()), suite.getModuleKey(), suite.getTitle());
        }
    }

    /**
     * 获取指定模块号的验收套件
     *
     * @param moduleId 模块号
     * @return 验收套件，未注册返回null
     */
    public ModuleAcceptanceSuite getSuite(int moduleId) {
        return suites.get(moduleId);
    }

    /**
     * 获取指定模块号的验收套件摘要，用于工作台目录轻量化展示
     *
     * @param moduleId 模块号
     * @return 结构化摘要字典（未注册返回 available=false）
     */
    public Map<String, Object> getSuiteSummary(int moduleId) {
        ModuleAcceptanceSuite suite = suites.get(moduleId);
        if (suite == null) {
            return Collections.singletonMap("available", false);
        }
        Map<String, Object> topology = inspectClusterTopology(suite);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("available", true);
        map.put("moduleId", suite.getModuleId());
        map.put("moduleKey", suite.getModuleKey());
        map.put("title", suite.getTitle());
        map.put("declaredBranches", suite.getDeclaredBranches());
        map.put("relatedTables", suite.getRelatedTables());
        map.put("requiredServerTypes", topology.get("requiredServerTypes"));
        map.put("clusterTopology", topology);
        return map;
    }

    /**
     * 列出所有已注册模块套件摘要
     *
     * @return 套件摘要列表
     */
    public List<Map<String, Object>> listSuiteSummaries() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ModuleAcceptanceSuite suite : suites.values()) {
            list.add(getSuiteSummary(suite.getModuleId()));
        }
        return list;
    }

    /**
     * 执行指定模块的双轨自动化验收（兼容离线JUnit与在线集群模式）
     *
     * @param moduleId 模块号
     * @param mode 验收运行模式（CONTRACT, MATRIX_SCAN, ALL）
     * @param caseId 指定重跑的单条用例ID（传空执行全量）
     * @param maxMatrixRows 最大配表扫描行数限制
     * @return 结构化验收汇总报告
     * @throws Exception 运行时异常或生产硬阻断
     */
    public ModuleAcceptanceReport runSuite(int moduleId, AcceptanceRunMode mode, String caseId, int maxMatrixRows) throws Exception {
        return runSuite(moduleId, mode, caseId, maxMatrixRows, isLiveServerRuntime());
    }

    /**
     * 执行指定模块的双轨自动化验收
     *
     * @param moduleId 模块号
     * @param mode 验收运行模式（CONTRACT, MATRIX_SCAN, ALL）
     * @param caseId 指定重跑的单条用例ID（传空执行全量）
     * @param maxMatrixRows 最大配表扫描行数限制
     * @param requireLiveCluster 是否强制要求真实服务器集群节点全部在线就绪（禁止退化为离线本地替身）
     * @return 结构化验收汇总报告
     * @throws Exception 运行时异常、集群未就绪或生产硬阻断
     */
    public ModuleAcceptanceReport runSuite(int moduleId, AcceptanceRunMode mode, String caseId, int maxMatrixRows, boolean requireLiveCluster) throws Exception {
        // 生产环境绝对硬阻断
        if (GmSecurityPolicy.currentRunMode().isProduction()) {
            throw new LogicException(ErrorMsgEnum.production_gm_not_allow.getId(), "生产环境禁止执行模块自动化验收");
        }

        ModuleAcceptanceSuite suite = suites.get(moduleId);
        if (suite == null) {
            throw new IllegalArgumentException("该模块尚未注册自动化验收套件: " + moduleId);
        }

        Map<String, Object> topology = inspectClusterTopology(suite);
        @SuppressWarnings("unchecked")
        List<String> missingTypes = (List<String>) topology.get("missingServerTypes");
        boolean isLive = Boolean.TRUE.equals(topology.get("liveServerRuntime"));
        if (requireLiveCluster && (!isLive || (missingTypes != null && !missingTypes.isEmpty()))) {
            throw new IllegalStateException("真实服务器集群环境未就绪: 缺少必需的在线服务节点 " + missingTypes
                    + " (liveServerRuntime=" + isLive + ")，请先启动对应的 GameServer/CrossServer/LoginServer 进程后再执行全面验收");
        }

        AcceptanceRunMode effectiveMode = mode != null ? mode : AcceptanceRunMode.ALL;

        // 获取或初始化沙盒隔离玩家
        long preferredId = PlayerSaveCoordinator.SANDBOX_PLAYER_ID_MIN + moduleId;
        Player sandboxPlayer = SandboxExecutionGuard.acquireSandboxPlayer(preferredId, moduleId);

        SandboxExecutionGuard.setSandboxThreadMuted(true);
        try {
            AcceptanceContext ctx = new AcceptanceContext(sandboxPlayer, effectiveMode, caseId, maxMatrixRows, suite.getDeclaredBranches());
            if (missingTypes != null && !missingTypes.isEmpty()) {
                ctx.addWarning("CLUSTER_TOPOLOGY_MISSING:" + String.join(",", missingTypes));
            }
            long startNs = System.nanoTime();

            // 1. 轨道一：功能契约与状态机边界用例
            if (effectiveMode == AcceptanceRunMode.CONTRACT || effectiveMode == AcceptanceRunMode.ALL) {
                try {
                    suite.runContractCases(ctx);
                } catch (Throwable t) {
                    log.warn("执行功能契约套件异常: moduleId=0x{}", Integer.toHexString(moduleId), t);
                    ctx.addWarning("契约用例套件抛出异常: " + t.getMessage());
                }
            }

            // 2. 轨道二：多态策略 × 配表全集扫描
            if ((effectiveMode == AcceptanceRunMode.MATRIX_SCAN || effectiveMode == AcceptanceRunMode.ALL) && !ctx.isCircuitBroken()) {
                try {
                    suite.runMatrixScan(ctx);
                } catch (Throwable t) {
                    log.warn("执行配表矩阵扫描套件异常: moduleId=0x{}", Integer.toHexString(moduleId), t);
                    ctx.addWarning("配表扫描套件抛出异常: " + t.getMessage());
                }
            }

            long totalElapsedMs = Math.max(0L, (System.nanoTime() - startNs) / 1_000_000L);
            return ctx.buildReport(suite.getModuleId(), suite.getModuleKey(), suite.getTitle(), totalElapsedMs, topology);
        } finally {
            if (sandboxPlayer != null) {
                SandboxExecutionGuard.enableConditionBypass(sandboxPlayer.getPlayerId(), false);
                SandboxExecutionGuard.unregisterLiveClusterRouting(sandboxPlayer.getPlayerId());
            }
            SandboxExecutionGuard.setSandboxThreadMuted(false);
        }
    }
}
