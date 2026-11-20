package cn.game.games.net.game.gm.acceptance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.exception.LogicException;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.gm.GmSecurityPolicy;
import cn.game.games.net.game.gm.acceptance.suites.ItemModuleAcceptanceSuite;
import cn.game.games.net.game.gm.acceptance.suites.ShopModuleAcceptanceSuite;
import cn.game.games.net.game.helper.PlayerSaveCoordinator;
import cn.game.protocol.manual.ErrorMsgEnum;

/**
 * 进程内模块自动化双轨验收引擎。
 * 负责套件生命周期注册、生产环境硬阻断、沙盒玩家上下文调度与多态配表矩阵扫描编排。
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
        // 自动注册内置两大标杆模块验收套件
        registerSuite(new ItemModuleAcceptanceSuite());
        registerSuite(new ShopModuleAcceptanceSuite());
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
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("available", true);
        map.put("moduleId", suite.getModuleId());
        map.put("moduleKey", suite.getModuleKey());
        map.put("title", suite.getTitle());
        map.put("declaredBranches", suite.getDeclaredBranches());
        map.put("relatedTables", suite.getRelatedTables());
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
     * 执行指定模块的双轨自动化验收
     *
     * @param moduleId 模块号
     * @param mode 验收运行模式（CONTRACT, MATRIX_SCAN, ALL）
     * @param caseId 指定重跑的单条用例ID（传空执行全量）
     * @param maxMatrixRows 最大配表扫描行数限制
     * @return 结构化验收汇总报告
     * @throws Exception 运行时异常或生产硬阻断
     */
    public ModuleAcceptanceReport runSuite(int moduleId, AcceptanceRunMode mode, String caseId, int maxMatrixRows) throws Exception {
        // 生产环境绝对硬阻断
        if (GmSecurityPolicy.currentRunMode().isProduction()) {
            throw new LogicException(ErrorMsgEnum.production_gm_not_allow.getId(), "生产环境禁止执行模块自动化验收");
        }

        ModuleAcceptanceSuite suite = suites.get(moduleId);
        if (suite == null) {
            throw new IllegalArgumentException("该模块尚未注册自动化验收套件: " + moduleId);
        }

        AcceptanceRunMode effectiveMode = mode != null ? mode : AcceptanceRunMode.ALL;

        // 获取或初始化沙盒隔离玩家
        long preferredId = PlayerSaveCoordinator.SANDBOX_PLAYER_ID_MIN + moduleId;
        Player sandboxPlayer = SandboxExecutionGuard.acquireSandboxPlayer(preferredId, moduleId);

        SandboxExecutionGuard.setSandboxThreadMuted(true);
        try {
            AcceptanceContext ctx = new AcceptanceContext(sandboxPlayer, effectiveMode, caseId, maxMatrixRows, suite.getDeclaredBranches());
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
            return ctx.buildReport(suite.getModuleId(), suite.getModuleKey(), suite.getTitle(), totalElapsedMs);
        } finally {
            SandboxExecutionGuard.setSandboxThreadMuted(false);
        }
    }
}
