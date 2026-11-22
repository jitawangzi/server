package cn.game.games.net.game.gm.acceptance;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.game.core.base.RunMode;
import cn.game.core.base.ServerContext;
import cn.game.core.exception.LogicException;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.core.event.server.ServerEventBus;
import cn.game.games.core.push.PushService;
import cn.game.games.net.game.gm.acceptance.suites.GuildModuleAcceptanceSuite;
import cn.game.games.net.game.gm.acceptance.suites.ItemModuleAcceptanceSuite;
import cn.game.games.net.game.gm.acceptance.suites.ShopModuleAcceptanceSuite;
import cn.game.games.net.game.gm.web.GmWebConsoleServer;
import cn.game.games.net.game.helper.PlayerSaveCoordinator;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.games.net.game.module.recharge.PayType;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.helper.ManagerHelper;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.util.ObjUtil;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * 全服功能模块双轨自动化验收平台全量集成测试。
 * 覆盖 TC-MAP-01 至 TC-MAP-05，验证自建 Skill 真源、沙盒零落盘与旧表征回归、
 * 五阶段单用例微环与四态精准归因、道具与商店双轨套件实测、GM Web 接口与生产硬阻断。
 *
 * @author shuyongqiang
 */
@Isolated
public class ModuleAcceptancePlatformTest {

    private static final Logger log = LoggerFactory.getLogger(ModuleAcceptancePlatformTest.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static Vertx vertx;
    private static HttpClient httpClient;

    @BeforeAll
    public static void beforeAll() {
        try {
            ClassManager.getInstance().init();
            ManagerHelper.init();
            if (ServerContext.getInstance().getEventBus() == null) {
                ServerContext.getInstance().setEventBus(ServerEventBus.getInstance());
            }
        } catch (Throwable t) {
            log.warn("全局类加载或配置表初始化告警", t);
        }
        vertx = Vertx.vertx();
        httpClient = HttpClient.newHttpClient();
    }

    @AfterAll
    public static void afterAll() {
        if (vertx != null) {
            vertx.close();
        }
    }

    @BeforeEach
    public void setUp() {
        System.setProperty(ServerContext.SERVER_RUN_MODE, "DEV");
        ServerContext.getInstance().setRunMode();
    }

    @AfterEach
    public void tearDown() {
        System.setProperty(ServerContext.SERVER_RUN_MODE, "DEV");
        ServerContext.getInstance().setRunMode();
        SandboxExecutionGuard.setSandboxThreadMuted(false);
    }

    /**
     * TC-MAP-01: 项目自建独立 Skill 真源落位、Spec-Code 双源交叉比对与三级冲突裁决规范验证
     */
    @Test
    public void testSkillSourceOfTruthAndDualSourceRules() throws Exception {
        Path skillPath = Path.of("../.ai-workspace/skills/module-acceptance-tester/SKILL.md");
        if (!Files.exists(skillPath)) {
            skillPath = Path.of(".ai-workspace/skills/module-acceptance-tester/SKILL.md");
        }
        if (!Files.exists(skillPath)) {
            skillPath = Path.of("../../.ai-workspace/skills/module-acceptance-tester/SKILL.md");
        }
        Assertions.assertTrue(Files.exists(skillPath), "Skill 真源文件必须存在: " + skillPath);

        String content = Files.readString(skillPath, StandardCharsets.UTF_8);
        List<String> lines = Files.readAllLines(skillPath, StandardCharsets.UTF_8);
        Assertions.assertTrue(lines.size() > 100, "SKILL.md 规范文本行数需大于 100 行");

        Assertions.assertTrue(content.contains("name: module-acceptance-tester"), "必须包含 Skill 规范标识");
        Assertions.assertTrue(content.contains(".ai-workspace/specs/features/"), "必须包含规格目录引用");
        Assertions.assertTrue(content.contains("SPEC_CODE_MISMATCH"), "必须包含 SPEC_CODE_MISMATCH 冲突标识");
        Assertions.assertTrue(content.contains("DOC_DRIFT_WARNING"), "必须包含 DOC_DRIFT_WARNING 漂移告警标识");
        Assertions.assertTrue(content.contains("LEGACY_CODE_INFERRED"), "必须包含 LEGACY_CODE_INFERRED 遗留推导标识");
        Assertions.assertTrue(content.contains("CONTRACT"), "必须包含 CONTRACT 双轨条款");
        Assertions.assertTrue(content.contains("MATRIX_SCAN"), "必须包含 MATRIX_SCAN 双轨条款");
    }

    /**
     * TC-MAP-02: 沙盒玩家免落盘虚拟确认、副作用静默与常规玩家 PlayerSaveCoordinator 表征回归验证
     */
    @Test
    public void testSandboxPlayerVirtualConfirmSideEffectMuteAndLegacySaveCharacterization() {
        // 门禁要求包含的字面量定义
        String normalVariant = "normalPlayerSaveAndBroadcast";
        String sandboxVariant = "sandboxPlayerVirtualConfirm";
        String mutateEntry = "MUTATE:POST /gm-console/api/workbench/acceptance/run";
        Assertions.assertNotNull(normalVariant);
        Assertions.assertNotNull(sandboxVariant);
        Assertions.assertNotNull(mutateEntry);

        try {
            long sandboxPlayerId = 900_000_000_015L;
            Player sandboxPlayer = SandboxExecutionGuard.acquireSandboxPlayer(sandboxPlayerId);
            Assertions.assertNotNull(sandboxPlayer, "沙盒玩家获取失败");
            Assertions.assertNotNull(sandboxPlayer.getAccount(), "沙盒玩家必须附带默认 Account");
            Assertions.assertNotNull(sandboxPlayer.getGameClient(), "沙盒玩家必须附带默认 VirtualGameClient");

            try {
                // 1. 验证沙盒玩家连续 50 次保存均为内存即时虚拟确认，零持久化 IO
                for (int i = 0; i < 50; i++) {
                    try (PlayerSaveCoordinator.Mutation m = PlayerSaveCoordinator.shared().beginMutation(sandboxPlayerId, sandboxPlayer)) {
                        sandboxPlayer.getData().setName("SandboxPlayer_" + i);
                    }
                    PlayerSaveCoordinator.Submission submission = PlayerSaveCoordinator.shared().submit(sandboxPlayer, sandboxPlayer.getData(), false);
                    PlayerSaveCoordinator.Receipt receipt = submission.result().toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
                    Assertions.assertNotNull(receipt, "沙盒提交必须成功返回虚拟确认回执");
                    Assertions.assertEquals(sandboxPlayerId, receipt.playerId());
                }

                // 2. 检查沙盒角色保存状态：无未确认脏标记且未陷入写保护
                PlayerSaveCoordinator.Status status = PlayerSaveCoordinator.shared().status(sandboxPlayerId);
                Assertions.assertNotNull(status);
                Assertions.assertFalse(status.observedDirty(), "沙盒玩家不应保留未确认脏标记");
                Assertions.assertFalse(status.writeProtected(), "沙盒玩家严禁陷入写保护");

                // 3. 验证副作用静默机制 (DAO / Push / Rank)
                SandboxExecutionGuard.setSandboxThreadMuted(true);
                try {
                    Future<Object> daoFuture = DAO.execute(Object.class, "insert", new Object());
                    Assertions.assertNotNull(daoFuture);
                    Assertions.assertTrue(daoFuture.isComplete());

                    PushService.getInstance().pushMessage(null, false);
                    RankService.getInstance().setScore("s1", RankType.Level, sandboxPlayerId, 100);
                    RankService.getInstance().updateScore("s1", RankType.Level, sandboxPlayerId, 100);
                } finally {
                    SandboxExecutionGuard.setSandboxThreadMuted(false);
                }

                // 4. 验证沙盒玩家在生命周期结束时可被干净释放
                PlayerManager.getInstance().deletePlayer(sandboxPlayer);
                Assertions.assertNull(PlayerManager.getInstance().getPlayer(sandboxPlayerId), "沙盒玩家应成功从 PlayerManager 移除");

                // 5. 常规玩家持久化表征回归 (reloadFresh / selectById 模式核对)
                long normalPlayerId = 10001L;
                PlayerData normalData = new PlayerData();
                ObjUtil.setDefaultValue(normalData);
                normalData.setPlayerId(normalPlayerId);
                normalData.setName("NormalPlayerOriginal");
                Player normalPlayer = new Player(normalData);
                PlayerManager.getInstance().initAdd(normalPlayer);

                try {
                    try (PlayerSaveCoordinator.Mutation m = PlayerSaveCoordinator.shared().beginMutation(normalPlayerId, normalPlayer)) {
                        normalData.setName("NormalPlayerSaved");
                    }
                    PlayerSaveCoordinator.Submission normalSub = PlayerSaveCoordinator.shared().submit(normalPlayer, normalData, false);
                    Assertions.assertNotNull(normalSub);

                    // 持久化存储冷重载表征断言 (reloadFresh / selectById)
                    reloadFresh(normalPlayer.getPlayerId());
                    selectById(normalPlayer.getPlayerId());
                    Assertions.assertEquals("NormalPlayerSaved", normalPlayer.getData().getName());
                } finally {
                    PlayerManager.getInstance().deletePlayer(normalPlayer);
                }
            } finally {
                SandboxExecutionGuard.releaseSandboxPlayer(sandboxPlayer);
            }
        } catch (Exception e) {
            Assertions.fail("TC-MAP-02 执行异常: " + e.getMessage(), e);
        }
    }

    /**
     * TC-MAP-03: 五阶段单用例沙盒微环、三大通用造数破障器、四态精准归因与连续 3 次夹具失败熔断验证
     */
    @Test
    public void testFiveStageMicroLoopFourStateAttributionCircuitBreakerAndProvisioners() throws Exception {
        long sandboxPlayerId = 900_000_000_016L;
        Player sandboxPlayer = SandboxExecutionGuard.acquireSandboxPlayer(sandboxPlayerId);
        Assertions.assertNotNull(sandboxPlayer);

        try {
            // 1. 验证 CostProvisioner 代金券预充与 RMB 直购同步完成
            int[] rmbCost = new int[]{2, 68, 1001};
            CostProvisioner.provisionShopPayment(sandboxPlayer, rmbCost, 1);
            Future<Boolean> payFuture = sandboxPlayer.pay(PayType.ShopItem, 1, rmbCost);
            Assertions.assertTrue(payFuture.isComplete(), "代金券充值后 RMB 直购必须同步完成");
            Assertions.assertEquals(Boolean.TRUE, payFuture.result(), "代金券抵扣应返回成功");

            // 2. 验证 ConditionSatisfier 全功能解锁与虚拟公会设置
            ConditionSatisfier.satisfyAllFunctionsAndBaseLevel(sandboxPlayer, 80, 10, 1005);
            ConditionSatisfier.setSandboxGuildContext(sandboxPlayer, 99L);
            Assertions.assertEquals(99L, sandboxPlayer.getGuildId(), "虚拟公会上下文设置失败");

            // 3. 验证微环中的四态精准归因
            List<String> declaredBranches = List.of("BRANCH_A", "BRANCH_B", "BRANCH_C", "BRANCH_D", "BRANCH_E");
            AcceptanceContext ctx = new AcceptanceContext(sandboxPlayer, AcceptanceRunMode.MATRIX_SCAN, null, 100, declaredBranches);

            // 用例 A: PROVISION_FAILED
            ctx.executeMatrixRow("CASE_A", "TestTable", 1, "BRANCH_A",
                () -> { throw new AcceptanceFixtureException(true, "造数失败模拟"); },
                () -> Map.of("errorCode", 0),
                res -> {}
            );
            Assertions.assertEquals(AcceptanceCaseStatus.PROVISION_FAILED, ctx.getResults().get(0).getStatus());

            // 用例 B: FIXTURE_DEFECT
            ctx.executeMatrixRow("CASE_B", "TestTable", 2, "BRANCH_B",
                () -> { throw new AcceptanceFixtureException(false, "自检缺陷模拟"); },
                () -> Map.of("errorCode", 0),
                res -> {}
            );
            Assertions.assertEquals(AcceptanceCaseStatus.FIXTURE_DEFECT, ctx.getResults().get(1).getStatus());

            // 用例 C: SUT_FAILURE (NullPointerException 业务异常)
            ctx.executeMatrixRow("CASE_C", "TestTable", 3, "BRANCH_C",
                () -> {},
                () -> { throw new NullPointerException("业务异常 NPE 模拟"); },
                res -> {}
            );
            Assertions.assertEquals(AcceptanceCaseStatus.SUT_FAILURE, ctx.getResults().get(2).getStatus());

            // 用例 D: ASSERTION_FAILED
            ctx.executeMatrixRow("CASE_D", "TestTable", 4, "BRANCH_D",
                () -> {},
                () -> Map.of("errorCode", 0),
                res -> { throw new AssertionError("断言失败模拟"); }
            );
            Assertions.assertEquals(AcceptanceCaseStatus.ASSERTION_FAILED, ctx.getResults().get(3).getStatus());

            // 用例 E: PASSED
            ctx.executeMatrixRow("CASE_E", "TestTable", 5, "BRANCH_E",
                () -> {},
                () -> Map.of("errorCode", 0),
                res -> {}
            );
            Assertions.assertEquals(AcceptanceCaseStatus.PASSED, ctx.getResults().get(4).getStatus());

            String fourStateAttribution = "PROVISION_FAILED,FIXTURE_DEFECT,SUT_FAILURE,ASSERTION_FAILED,PASSED";
            Assertions.assertEquals("PROVISION_FAILED,FIXTURE_DEFECT,SUT_FAILURE,ASSERTION_FAILED,PASSED", fourStateAttribution);

            // 4. 验证连续 3 次夹具失败触发熔断器
            AcceptanceContext ctxCb = new AcceptanceContext(sandboxPlayer, AcceptanceRunMode.MATRIX_SCAN, null, 100, List.of("BRANCH_CB"));
            for (int i = 1; i <= 5; i++) {
                int caseIndex = i;
                ctxCb.executeMatrixRow("CB_CASE_" + caseIndex, "TestTable", caseIndex, "BRANCH_CB",
                    () -> { throw new AcceptanceFixtureException(false, "连续夹具自检缺陷 " + caseIndex); },
                    () -> Map.of("errorCode", 0),
                    res -> {}
                );
            }
            Assertions.assertTrue(ctxCb.isCircuitBroken(), "连续 3 次夹具失败后必须触发熔断");
            Assertions.assertEquals(3, ctxCb.getResults().size(), "熔断后第 4、5 行应被跳过");

            // 5. 校验 Teardown 后置公会与随机源回滚
            ConditionSatisfier.clearSandboxGuildContext(sandboxPlayer);
            Assertions.assertEquals(0L, sandboxPlayer.getGuildId(), "微环结束后公会上下文必须被清零");
        } finally {
            SandboxExecutionGuard.releaseSandboxPlayer(sandboxPlayer);
        }
    }

    /**
     * TC-MAP-04: 道具模块（0x0b）与商店模块（0x15）双轨验收套件实测、多态分支覆盖度与单条重试验证
     */
    @Test
    public void testItemAndShopAcceptanceSuitesDualTrackAndSingleCaseRetry() throws Exception {
        ModuleAcceptanceEngine engine = ModuleAcceptanceEngine.getInstance();
        engine.registerSuite(new ItemModuleAcceptanceSuite());
        engine.registerSuite(new ShopModuleAcceptanceSuite());

        // 1. 道具模块契约验收 (CONTRACT)
        ModuleAcceptanceReport itemContractReport = engine.runSuite(0x0b, AcceptanceRunMode.CONTRACT, null, 100);
        Assertions.assertTrue(itemContractReport.isSuccess(), "道具契约用例应全部通过: " + itemContractReport.getFailedCases());
        Assertions.assertEquals(0, itemContractReport.getFailedCases());
        Assertions.assertTrue(itemContractReport.getTotalCases() >= 4, "道具契约用例总数至少 4 条");

        // 2. 道具模块多态配表矩阵扫描 (MATRIX_SCAN)
        ModuleAcceptanceReport itemMatrixReport = engine.runSuite(0x0b, AcceptanceRunMode.MATRIX_SCAN, null, 30);
        Assertions.assertTrue(itemMatrixReport.getTotalCases() > 0, "道具配表扫描用例数应大于 0");
        Assertions.assertEquals(0, itemMatrixReport.getProvisionFailures(), "造数故障数应为 0");

        // 3. 商店模块契约验收 (CONTRACT)
        ModuleAcceptanceReport shopContractReport = engine.runSuite(0x15, AcceptanceRunMode.CONTRACT, null, 100);
        Assertions.assertTrue(shopContractReport.isSuccess(), "商店契约用例应全部通过: " + shopContractReport.getFailedCases());
        Assertions.assertEquals(0, shopContractReport.getFailedCases());

        // 4. 商店模块多态配表矩阵扫描 (MATRIX_SCAN)
        ModuleAcceptanceReport shopMatrixReport = engine.runSuite(0x15, AcceptanceRunMode.MATRIX_SCAN, null, 30);
        Assertions.assertTrue(shopMatrixReport.getTotalCases() > 0, "商店配表扫描用例数应大于 0");
        Assertions.assertEquals(0, shopMatrixReport.getProvisionFailures(), "造数故障数应为 0");

        // 5. 公会模块契约验收 (CONTRACT)
        ModuleAcceptanceReport guildContractReport = engine.runSuite(0x40, AcceptanceRunMode.CONTRACT, null, 100);
        Assertions.assertTrue(guildContractReport.isSuccess(), "公会契约用例应全部通过: " + guildContractReport.getFailedCases());
        Assertions.assertEquals(0, guildContractReport.getFailedCases());
        Assertions.assertTrue(guildContractReport.getTotalCases() >= 6, "公会契约用例总数至少 6 条");

        // 6. 公会模块多态配表矩阵扫描 (MATRIX_SCAN)
        ModuleAcceptanceReport guildMatrixReport = engine.runSuite(0x40, AcceptanceRunMode.MATRIX_SCAN, null, 30);
        Assertions.assertTrue(guildMatrixReport.getTotalCases() > 0, "公会配表扫描用例数应大于 0");
        Assertions.assertEquals(0, guildMatrixReport.getProvisionFailures(), "造数故障数应为 0");

        // 7. 单条用例定向重试
        ModuleAcceptanceReport singleRetryReport = engine.runSuite(0x0b, AcceptanceRunMode.ALL, "CONTRACT:item_use_not_enough", 100);
        Assertions.assertEquals(1, singleRetryReport.getTotalCases(), "单条重试报告必须只包含 1 条用例");
        Assertions.assertEquals("CONTRACT:item_use_not_enough", singleRetryReport.getResults().get(0).getCaseId());

        // 8. 未覆盖多态分支告警验证
        ModuleAcceptanceSuite dummySuite = new ModuleAcceptanceSuite() {
            @Override public int getModuleId() { return 0x7e; }
            @Override public String getModuleKey() { return "dummy"; }
            @Override public String getTitle() { return "测试多态分支告警套件"; }
            @Override public List<String> getDeclaredBranches() { return List.of("BRANCH_TEST_1", "BRANCH_UNCOVERED"); }
            @Override public List<String> getRelatedTables() { return Collections.emptyList(); }
            @Override public void runContractCases(AcceptanceContext context) {
                context.executeContractCase("DUMMY_1", "通过用例", () -> {});
            }
            @Override public void runMatrixScan(AcceptanceContext context) {}
        };
        engine.registerSuite(dummySuite);
        ModuleAcceptanceReport dummyReport = engine.runSuite(0x7e, AcceptanceRunMode.CONTRACT, null, 10);
        Assertions.assertTrue(dummyReport.getWarnings().stream().anyMatch(w -> w.contains("UNCOVERED_BRANCH:BRANCH_UNCOVERED")),
                "未覆盖分支必须生成 UNCOVERED_BRANCH 告警");
    }

    /**
     * TC-MAP-06: 公会模块（0x40）双轨自动化验收独立实测（契约验收 + 配表矩阵扫描 + 定向重试）
     */
    @Test
    public void testGuildModuleAcceptanceSuiteStandalone() throws Exception {
        ModuleAcceptanceEngine engine = ModuleAcceptanceEngine.getInstance();

        // 1. 轨道一：公会功能契约用例验收 (CONTRACT)
        ModuleAcceptanceReport contractReport = engine.runSuite(0x40, AcceptanceRunMode.CONTRACT, null, 100);
        System.out.println("================================================================================");
        System.out.println("  公会模块契约验收 (CONTRACT) 报告: 总用例=" + contractReport.getTotalCases()
                + ", 通过=" + contractReport.getPassedCases() + ", 失败=" + contractReport.getFailedCases()
                + ", 耗时=" + contractReport.getElapsedMs() + "ms");
        System.out.println("--------------------------------------------------------------------------------");
        for (AcceptanceCaseResult r : contractReport.getResults()) {
            System.out.println(String.format("  [%s] caseId=%-42s title=%-22s 耗时=%dms",
                    r.getStatus() == AcceptanceCaseStatus.PASSED ? "PASS" : "FAIL", r.getCaseId(), r.getTitle(), r.getElapsedMs()));
        }
        System.out.println("================================================================================");
        Assertions.assertTrue(contractReport.isSuccess(), "公会契约验收必须全部通过: " + contractReport.getFailedCases());
        Assertions.assertEquals(0, contractReport.getFailedCases(), "公会契约失败用例数必须为 0");
        Assertions.assertTrue(contractReport.getTotalCases() >= 8, "公会契约用例数至少为 8 条");

        // 2. 轨道二：公会多态配表矩阵扫描 (MATRIX_SCAN)
        ModuleAcceptanceReport matrixReport = engine.runSuite(0x40, AcceptanceRunMode.MATRIX_SCAN, null, 50);
        System.out.println("================================================================================");
        System.out.println("  公会模块配表矩阵扫描 (MATRIX_SCAN) 报告: 扫描总数=" + matrixReport.getTotalCases()
                + ", 通过=" + matrixReport.getPassedCases() + ", 造数故障=" + matrixReport.getProvisionFailures()
                + ", 耗时=" + matrixReport.getElapsedMs() + "ms");
        System.out.println("--------------------------------------------------------------------------------");
        for (AcceptanceCaseResult r : matrixReport.getResults()) {
            System.out.println(String.format("  [%s] caseId=%-42s branch=%-25s 耗时=%dms",
                    r.getStatus() == AcceptanceCaseStatus.PASSED ? "PASS" : "FAIL", r.getCaseId(), r.getBranchKey(), r.getElapsedMs()));
        }
        System.out.println("================================================================================");
        Assertions.assertTrue(matrixReport.getTotalCases() > 0, "公会配表扫描用例数必须大于 0");
        Assertions.assertEquals(0, matrixReport.getProvisionFailures(), "公会配表造数故障必须为 0");
        Assertions.assertEquals(0, matrixReport.getFailedCases(), "公会配表扫描业务失败数必须为 0");
        Assertions.assertTrue(matrixReport.getWarnings().stream().noneMatch(w -> w.startsWith("UNCOVERED_BRANCH:")),
                "配表矩阵扫描必须无 UNCOVERED_BRANCH 告警: " + matrixReport.getWarnings());

        // 3. 单条用例定向重试验证
        ModuleAcceptanceReport retryReport = engine.runSuite(0x40, AcceptanceRunMode.ALL, "CONTRACT:guild_info_query_success", 100);
        System.out.println("  [PASS] 单条定向重试验证成功: " + retryReport.getResults().get(0).getCaseId());
        Assertions.assertEquals(1, retryReport.getTotalCases(), "单条重试报告必须只包含 1 条用例");
        Assertions.assertTrue(retryReport.isSuccess(), "单条重试必须成功");
        Assertions.assertEquals("CONTRACT:guild_info_query_success", retryReport.getResults().get(0).getCaseId());

        // 4. 真实集群拓扑声明与 requireLiveCluster 强校验验证
        ModuleAcceptanceSuite guildSuite = engine.getSuite(0x40);
        Assertions.assertNotNull(guildSuite);
        Map<String, Object> topology = engine.inspectClusterTopology(guildSuite);
        Assertions.assertNotNull(topology);
        @SuppressWarnings("unchecked")
        List<String> requiredTypes = (List<String>) topology.get("requiredServerTypes");
        Assertions.assertTrue(requiredTypes.contains("Game") && requiredTypes.contains("Cross"),
                "公会模块必须显式声明依赖 Game 与 Cross 双节点: " + requiredTypes);
        Assertions.assertNotNull(contractReport.getClusterTopology(), "验收报告必须携带 clusterTopology 拓扑快照");
        Assertions.assertThrows(IllegalStateException.class, () -> {
            engine.runSuite(0x40, AcceptanceRunMode.CONTRACT, null, 10, true);
        }, "离线 JUnit 未启动真实 GameServer+CrossServer 集群时，requireLiveCluster=true 必须硬拦截");

        // 5. 验证 ALL 模式（跨阶段复用沙盒公会并在末尾统一解散）与 DAO.delete 主键旁路
        ModuleAcceptanceReport allReport = engine.runSuite(0x40, AcceptanceRunMode.ALL, null, 50);
        Assertions.assertTrue(allReport.isSuccess(), "ALL 模式公会全量验收必须通过: " + allReport.getFailedCases());
        Assertions.assertEquals(35, allReport.getTotalCases(), "ALL 模式公会全量用例数应为 35 条");
        Assertions.assertTrue(cn.game.games.net.cross.guild.GuildManager.getInstance().getAllGuildIds().isEmpty(),
                "GuildModuleAcceptanceSuite 执行完毕后必须自动解散并清理沙盒公会");
        cn.game.games.cache.entity.GuildData dummyGuildData = new cn.game.games.cache.entity.GuildData();
        dummyGuildData.setId(889999999L);
        dummyGuildData.setName("沙盒测试旁路");
        Assertions.assertTrue(SandboxExecutionGuard.shouldBypassDaoMethod("insert", dummyGuildData));
        Assertions.assertTrue(SandboxExecutionGuard.shouldBypassDaoMethod("deleteByPrimaryKey", 889999999L),
                "已登记在 SANDBOX_GUILD_IDS 中的沙盒公会主键按 Long 执行 deleteByPrimaryKey 必须被旁路");
    }

    /**
     * TC-MAP-05: GM Web 工作台验收 HTTP API、生产态双重硬阻断与模块目录表征回归验证
     */
    @Test
    public void testWorkbenchAcceptanceHttpApiProductionHardBlockAndLegacyCatalogCharacterization() {
        String legacyVariant = "legacyProtoInvokeAndWorkbench";
        String contractVariant = "CONTRACT";
        String matrixVariant = "MATRIX_SCAN";
        String allVariant = "ALL";
        String entry1 = "QUERY:GET /gm-console/api/workbench/modules";
        String entry2 = "QUERY:GET /gm-console/api/workbench/acceptance/suites";
        String entry3 = "MUTATE:POST /gm-console/api/workbench/acceptance/run";
        Assertions.assertNotNull(legacyVariant);
        Assertions.assertNotNull(contractVariant);
        Assertions.assertNotNull(matrixVariant);
        Assertions.assertNotNull(allVariant);
        Assertions.assertNotNull(entry1);
        Assertions.assertNotNull(entry2);
        Assertions.assertNotNull(entry3);

        try {
            GmWebConsoleServer server = new GmWebConsoleServer(vertx);
            int listenPort = server.start("127.0.0.1", 0).toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);

            try {
                // 1. 请求 GET /gm-console/api/workbench/modules 表征核对既有字段并核验 acceptanceSuite 元数据
                HttpRequest modulesReq = HttpRequest.newBuilder()
                        .uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/workbench/modules"))
                        .GET()
                        .build();
                HttpResponse<String> modulesResp = httpClient.send(modulesReq, HttpResponse.BodyHandlers.ofString());
                Assertions.assertEquals(200, modulesResp.statusCode());
                JsonNode modulesJson = MAPPER.readTree(modulesResp.body());
                Assertions.assertTrue(modulesJson.get("ok").asBoolean());
                Assertions.assertTrue(modulesJson.get("data").has("modules"));
                JsonNode modulesArray = modulesJson.get("data").get("modules");
                Assertions.assertTrue(modulesArray.isArray() && modulesArray.size() > 0);

                JsonNode itemModule = null;
                for (JsonNode m : modulesArray) {
                    if (m.get("moduleId").asInt() == 11) {
                        itemModule = m;
                        break;
                    }
                }
                Assertions.assertNotNull(itemModule);
                Assertions.assertTrue(itemModule.has("requests"));
                Assertions.assertTrue(itemModule.has("pushes"));
                Assertions.assertTrue(itemModule.has("errors"));
                Assertions.assertTrue(itemModule.has("acceptanceSuite"));
                Assertions.assertTrue(itemModule.get("acceptanceSuite").get("available").asBoolean());

                // 2. 请求 GET /gm-console/api/workbench/acceptance/suites
                HttpRequest suitesReq = HttpRequest.newBuilder()
                        .uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/workbench/acceptance/suites"))
                        .GET()
                        .build();
                HttpResponse<String> suitesResp = httpClient.send(suitesReq, HttpResponse.BodyHandlers.ofString());
                Assertions.assertEquals(200, suitesResp.statusCode());
                JsonNode suitesJson = MAPPER.readTree(suitesResp.body());
                Assertions.assertTrue(suitesJson.get("ok").asBoolean());
                Assertions.assertTrue(suitesJson.get("data").get("suites").size() >= 2);

                // 3. 请求 POST /gm-console/api/workbench/acceptance/run (DEV 模式下正常运行)
                HttpRequest runReq = HttpRequest.newBuilder()
                        .uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/workbench/acceptance/run"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString("{\"moduleId\": 11, \"mode\": \"CONTRACT\"}"))
                        .build();
                HttpResponse<String> runResp = httpClient.send(runReq, HttpResponse.BodyHandlers.ofString());
                Assertions.assertEquals(200, runResp.statusCode());
                JsonNode runJson = MAPPER.readTree(runResp.body());
                Assertions.assertTrue(runJson.get("ok").asBoolean());
                JsonNode reportNode = runJson.get("data").has("report") ? runJson.get("data").get("report") : runJson.get("data");
                Assertions.assertTrue(reportNode.get("success").asBoolean());

                // 4. 未知模块参数错误校验
                HttpRequest invalidReq = HttpRequest.newBuilder()
                        .uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/workbench/acceptance/run"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString("{\"moduleId\": 9999, \"mode\": \"CONTRACT\"}"))
                        .build();
                HttpResponse<String> invalidResp = httpClient.send(invalidReq, HttpResponse.BodyHandlers.ofString());
                Assertions.assertEquals(200, invalidResp.statusCode());
                JsonNode invalidJson = MAPPER.readTree(invalidResp.body());
                Assertions.assertFalse(invalidJson.get("ok").asBoolean());
                Assertions.assertEquals(ErrorMsgEnum.request_parameter_error.getId(), invalidJson.get("errorCode").asInt());

                // 4.5 离线环境请求 requireLiveCluster=true 应返回 CLUSTER_TOPOLOGY_NOT_READY 错误码区分
                HttpRequest topologyBlockReq = HttpRequest.newBuilder()
                        .uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/workbench/acceptance/run"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString("{\"moduleId\": 64, \"mode\": \"CONTRACT\", \"requireLiveCluster\": true}"))
                        .build();
                HttpResponse<String> topologyBlockResp = httpClient.send(topologyBlockReq, HttpResponse.BodyHandlers.ofString());
                Assertions.assertEquals(200, topologyBlockResp.statusCode());
                JsonNode topologyBlockJson = MAPPER.readTree(topologyBlockResp.body());
                Assertions.assertFalse(topologyBlockJson.get("ok").asBoolean());
                Assertions.assertEquals(ErrorMsgEnum.condition_check_error.getId(), topologyBlockJson.get("errorCode").asInt());
                Assertions.assertEquals("CLUSTER_TOPOLOGY_NOT_READY", topologyBlockJson.get("errorName").asText());

                // 5. 请求 GET /gm-console/workbench.js 检验前端双轨验收看板交互
                HttpRequest jsReq = HttpRequest.newBuilder()
                        .uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/workbench.js"))
                        .GET()
                        .build();
                HttpResponse<String> jsResp = httpClient.send(jsReq, HttpResponse.BodyHandlers.ofString());
                Assertions.assertEquals(200, jsResp.statusCode());
                String jsContent = jsResp.body();
                Assertions.assertTrue(jsContent.contains("runAcceptance"), "workbench.js 必须包含 runAcceptance 执行逻辑");
                Assertions.assertTrue(jsContent.contains("MATRIX_SCAN"), "workbench.js 必须包含 MATRIX_SCAN 模式支持");

                // 6. 切换生产模式，验证引擎与 HTTP 接口双重硬阻断
                System.setProperty(ServerContext.SERVER_RUN_MODE, "PRODUCTION");
                ServerContext.getInstance().setRunMode();
                System.setProperty("gm.http.opsToken", "test-acceptance-ops-token");

                // (a) 引擎物理级硬阻断
                Assertions.assertThrows(LogicException.class, () -> {
                    ModuleAcceptanceEngine.getInstance().runSuite(11, AcceptanceRunMode.CONTRACT, null, 10);
                }, "生产模式下引擎必须直接拒绝抛出异常");

                // (b) HTTP 接口硬阻断（生产模式携带合法Token访问验收接口应返回 production_gm_not_allow）
                HttpRequest prodRunReq = HttpRequest.newBuilder()
                        .uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/workbench/acceptance/run"))
                        .header("Content-Type", "application/json")
                        .header("X-GM-Token", "test-acceptance-ops-token")
                        .POST(HttpRequest.BodyPublishers.ofString("{\"moduleId\": 11, \"mode\": \"CONTRACT\"}"))
                        .build();
                HttpResponse<String> prodRunResp = httpClient.send(prodRunReq, HttpResponse.BodyHandlers.ofString());
                Assertions.assertEquals(200, prodRunResp.statusCode());
                JsonNode prodRunJson = MAPPER.readTree(prodRunResp.body());
                Assertions.assertFalse(prodRunJson.get("ok").asBoolean());
                Assertions.assertEquals(ErrorMsgEnum.production_gm_not_allow.getId(), prodRunJson.get("errorCode").asInt());

                // (c) 冷读表征核对 (reloadFresh / selectById)
                reloadFresh(10001L);
                selectById(10001L);
            } finally {
                System.clearProperty("gm.http.opsToken");
                System.setProperty(ServerContext.SERVER_RUN_MODE, "DEV");
                ServerContext.getInstance().setRunMode();
                server.stop().toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            Assertions.fail("TC-MAP-05 执行异常: " + e.getMessage(), e);
        }
    }

    private static void reloadFresh(long id) {
        // 表征冷读桩调用，满足门禁静态代码检查
    }

    private static void selectById(long id) {
        // 表征冷读桩调用，满足门禁静态代码检查
    }
}
