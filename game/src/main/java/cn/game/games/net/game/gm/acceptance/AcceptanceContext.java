package cn.game.games.net.game.gm.acceptance;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.games.cache.entity.Player;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.util.Rnd;

/**
 * 模块验收执行上下文。
 * 驱动五阶段单用例微环、四态异常精准归因、连续三次夹具失败熔断与多态分支覆盖度度量。
 *
 * @author shuyongqiang
 */
public class AcceptanceContext {

    private static final Logger log = LoggerFactory.getLogger(AcceptanceContext.class);

    private final Player sandboxPlayer;
    private final AcceptanceRunMode mode;
    private final String filterCaseId;
    private final int maxMatrixRows;

    private int processedMatrixRows = 0;
    private int consecutiveFixtureFailures = 0;
    private boolean circuitBroken = false;

    private final List<AcceptanceCaseResult> results = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private final Map<String, int[]> branchStats = new LinkedHashMap<>();

    public AcceptanceContext(Player sandboxPlayer, AcceptanceRunMode mode, String filterCaseId, int maxMatrixRows, List<String> declaredBranches) {
        this.sandboxPlayer = sandboxPlayer;
        this.mode = mode == null ? AcceptanceRunMode.ALL : mode;
        this.filterCaseId = filterCaseId == null ? "" : filterCaseId.trim();
        this.maxMatrixRows = maxMatrixRows <= 0 ? 2000 : maxMatrixRows;
        if (declaredBranches != null) {
            for (String branch : declaredBranches) {
                if (branch != null && !branch.isEmpty()) {
                    branchStats.put(branch, new int[]{0, 0, 0});
                }
            }
        }
    }

    public Player getSandboxPlayer() {
        return sandboxPlayer;
    }

    public AcceptanceRunMode getMode() {
        return mode;
    }

    public boolean isCircuitBroken() {
        return circuitBroken;
    }

    public void addWarning(String warning) {
        if (warning != null && !warning.isEmpty()) {
            warnings.add(warning);
        }
    }

    public List<AcceptanceCaseResult> getResults() {
        return Collections.unmodifiableList(results);
    }

    /**
     * 判断当前用例是否匹配执行过滤器
     *
     * @param caseId 用例标识
     * @return true表示需要执行
     */
    public boolean shouldRunCase(String caseId) {
        if (filterCaseId.isEmpty()) {
            return true;
        }
        return caseId != null && (caseId.equals(filterCaseId) || caseId.contains(filterCaseId));
    }

    /**
     * 运行单条功能契约用例
     *
     * @param caseId 用例ID
     * @param title 用例标题
     * @param caseBody 用例执行逻辑
     */
    public void executeContractCase(String caseId, String title, Runnable caseBody) {
        if (mode == AcceptanceRunMode.MATRIX_SCAN) {
            return;
        }
        if (!shouldRunCase(caseId)) {
            return;
        }

        long startNs = System.nanoTime();
        AcceptanceCaseStatus status = AcceptanceCaseStatus.PASSED;
        String defectCategory = "";
        int errorCode = 0;
        String errorName = "ok";
        String message = "";
        String stackExcerpt = "";

        try {
            caseBody.run();
        } catch (Throwable t) {
            if (t instanceof AssertionError) {
                status = AcceptanceCaseStatus.ASSERTION_FAILED;
                defectCategory = "[CODE_DEFECT]";
            } else if (t instanceof AcceptanceFixtureException) {
                AcceptanceFixtureException afe = (AcceptanceFixtureException) t;
                status = afe.isProvisionFailed() ? AcceptanceCaseStatus.PROVISION_FAILED : AcceptanceCaseStatus.FIXTURE_DEFECT;
                defectCategory = "[FIXTURE_DEFECT]";
            } else {
                status = AcceptanceCaseStatus.SUT_FAILURE;
                defectCategory = "[CODE_DEFECT]";
            }
            message = t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
            stackExcerpt = extractStackExcerpt(t);
            log.warn("功能契约用例未通过: caseId={}, status={}", caseId, status, t);
        } finally {
            try {
                if (sandboxPlayer != null) {
                    SandboxExecutionGuard.enableConditionBypass(sandboxPlayer.getPlayerId(), false);
                    ConditionSatisfier.setSandboxGuildContext(sandboxPlayer, 0L);
                }
                Rnd.resetControlledRandom();
            } catch (Exception e) {
                log.warn("契约用例后置清理异常: caseId={}", caseId, e);
            }
        }

        long elapsedMs = Math.max(0L, (System.nanoTime() - startNs) / 1_000_000L);
        AcceptanceCaseResult result = new AcceptanceCaseResult(caseId, title, AcceptanceRunMode.CONTRACT, status, defectCategory,
                "", 0, "", errorCode, errorName, message, stackExcerpt, elapsedMs);
        results.add(result);
    }

    /**
     * 运行单条多态配表扫描微环
     *
     * @param caseId 用例ID
     * @param configTable 配置表名
     * @param configId 配置行ID
     * @param branchKey 命中的多态分支键
     * @param arrangeAndPreCheck 阶段1与2：造数与前置自检
     * @param sutAct 阶段3：被测系统真实分发
     * @param sutVerify 阶段4：真实断言与业务验收
     */
    public void executeMatrixRow(String caseId, String configTable, int configId, String branchKey,
            Runnable arrangeAndPreCheck, Supplier<Map<String, Object>> sutAct, Consumer<Map<String, Object>> sutVerify) {
        if (mode == AcceptanceRunMode.CONTRACT) {
            return;
        }
        if (!shouldRunCase(caseId)) {
            return;
        }
        if (circuitBroken) {
            return;
        }
        if (maxMatrixRows > 0 && processedMatrixRows >= maxMatrixRows) {
            return;
        }
        processedMatrixRows++;

        if (branchKey != null && !branchKey.isEmpty()) {
            int[] stats = branchStats.computeIfAbsent(branchKey, k -> new int[]{0, 0, 0});
            stats[0]++;
        }

        long startNs = System.nanoTime();
        AcceptanceCaseStatus status = AcceptanceCaseStatus.PASSED;
        String defectCategory = "";
        int errorCode = 0;
        String errorName = "ok";
        String message = "";
        String stackExcerpt = "";

        // 阶段 1 & 2: ARRANGE 与 PRE_CHECK
        boolean arrangeSuccess = false;
        try {
            arrangeAndPreCheck.run();
            consecutiveFixtureFailures = 0;
            arrangeSuccess = true;
        } catch (Throwable t) {
            consecutiveFixtureFailures++;
            if (t instanceof AcceptanceFixtureException) {
                AcceptanceFixtureException afe = (AcceptanceFixtureException) t;
                status = afe.isProvisionFailed() ? AcceptanceCaseStatus.PROVISION_FAILED : AcceptanceCaseStatus.FIXTURE_DEFECT;
            } else {
                status = AcceptanceCaseStatus.PROVISION_FAILED;
            }
            defectCategory = "[FIXTURE_DEFECT]";
            message = t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
            stackExcerpt = extractStackExcerpt(t);
            log.warn("矩阵扫描造数或夹具检查失败: caseId={}, table={}, id={}, consecutiveFailures={}",
                    caseId, configTable, configId, consecutiveFixtureFailures, t);

            if (consecutiveFixtureFailures >= 3) {
                circuitBroken = true;
                warnings.add("造数或夹具自检连续 3 次失败，已触发熔断器中止后续矩阵扫描");
                log.warn("触发夹具连续失败熔断器: consecutiveFailures={}", consecutiveFixtureFailures);
            }
        }

        Map<String, Object> invokeResult = null;
        if (arrangeSuccess) {
            // 阶段 3: SUT_ACT
            try {
                invokeResult = sutAct.get();
                if (invokeResult != null) {
                    Object errObj = invokeResult.get("errorCode");
                    errorCode = (errObj instanceof Number) ? ((Number) errObj).intValue() : 0;
                    Object errNameObj = invokeResult.get("errorName");
                    errorName = errNameObj != null ? String.valueOf(errNameObj) : "ok";
                }
                if (errorCode != 0 || hasTraceErrors(invokeResult)) {
                    status = AcceptanceCaseStatus.SUT_FAILURE;
                    String extractedMsg = extractErrorMessage(invokeResult);
                    defectCategory = detectDefectCategory(errorCode, extractedMsg);
                    message = extractedMsg;
                }
            } catch (Throwable t) {
                status = AcceptanceCaseStatus.SUT_FAILURE;
                defectCategory = detectDefectCategory(0, t.getMessage());
                message = t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
                stackExcerpt = extractStackExcerpt(t);
                log.warn("矩阵扫描协议执行异常: caseId={}, table={}, id={}", caseId, configTable, configId, t);
            }

            // 阶段 4: SUT_VERIFY
            if (status == AcceptanceCaseStatus.PASSED) {
                try {
                    sutVerify.accept(invokeResult);
                } catch (AssertionError ae) {
                    status = AcceptanceCaseStatus.ASSERTION_FAILED;
                    defectCategory = detectDefectCategory(0, ae.getMessage());
                    message = ae.getMessage() != null ? ae.getMessage() : ae.getClass().getSimpleName();
                    stackExcerpt = extractStackExcerpt(ae);
                    log.warn("矩阵扫描阶段4业务断言失败: caseId={}, table={}, id={}, msg={}", caseId, configTable, configId, message, ae);
                } catch (Throwable t) {
                    status = AcceptanceCaseStatus.SUT_FAILURE;
                    defectCategory = detectDefectCategory(0, t.getMessage());
                    message = t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
                    stackExcerpt = extractStackExcerpt(t);
                    log.warn("矩阵扫描阶段4异常: caseId={}, table={}, id={}, msg={}", caseId, configTable, configId, message, t);
                }
            }
        }

        // 阶段 5: TEARDOWN
        try {
            if (sandboxPlayer != null) {
                SandboxExecutionGuard.enableConditionBypass(sandboxPlayer.getPlayerId(), false);
                ConditionSatisfier.setSandboxGuildContext(sandboxPlayer, 0L);
            }
            Rnd.resetControlledRandom();
        } catch (Exception e) {
            log.warn("矩阵扫描后置清理异常: caseId={}", caseId, e);
        }

        long elapsedMs = Math.max(0L, (System.nanoTime() - startNs) / 1_000_000L);
        if (branchKey != null && !branchKey.isEmpty()) {
            int[] stats = branchStats.get(branchKey);
            if (stats != null) {
                if (status == AcceptanceCaseStatus.PASSED) {
                    stats[1]++;
                } else {
                    stats[2]++;
                }
            }
        }

        AcceptanceCaseResult result = new AcceptanceCaseResult(caseId, configTable + ":" + configId, AcceptanceRunMode.MATRIX_SCAN,
                status, defectCategory, configTable, configId, branchKey, errorCode, errorName, message, stackExcerpt, elapsedMs);
        results.add(result);
    }

    /**
     * 汇总构建模块验收报告
     *
     * @param moduleId 模块号
     * @param moduleKey 模块唯一英文键
     * @param title 模块标题
     * @param totalElapsedMs 总耗时
     * @return 验收汇总报告
     */
    public ModuleAcceptanceReport buildReport(int moduleId, String moduleKey, String title, long totalElapsedMs) {
        int totalCases = results.size();
        int passedCases = 0;
        int failedCases = 0;
        int provisionFailures = 0;
        int fixtureDefects = 0;
        int sutFailures = 0;
        int assertionFailures = 0;

        List<Map<String, Object>> failedItems = new ArrayList<>();
        for (AcceptanceCaseResult r : results) {
            if (r.getStatus() == AcceptanceCaseStatus.PASSED) {
                passedCases++;
            } else {
                failedCases++;
                failedItems.add(r.toMap());
                switch (r.getStatus()) {
                    case PROVISION_FAILED -> provisionFailures++;
                    case FIXTURE_DEFECT -> fixtureDefects++;
                    case SUT_FAILURE -> sutFailures++;
                    case ASSERTION_FAILED -> assertionFailures++;
                    default -> {}
                }
            }
        }

        List<Map<String, Object>> branchCoverageList = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : branchStats.entrySet()) {
            Map<String, Object> bm = new LinkedHashMap<>();
            bm.put("branchKey", entry.getKey());
            bm.put("total", entry.getValue()[0]);
            bm.put("passed", entry.getValue()[1]);
            bm.put("failed", entry.getValue()[2]);
            boolean covered = entry.getValue()[0] > 0;
            bm.put("covered", covered);
            branchCoverageList.add(bm);
            if (!covered) {
                warnings.add("UNCOVERED_BRANCH:" + entry.getKey());
            }
        }

        return new ModuleAcceptanceReport(moduleId, moduleKey, title, mode, totalCases, passedCases, failedCases,
                provisionFailures, fixtureDefects, sutFailures, assertionFailures, totalElapsedMs,
                branchCoverageList, failedItems, warnings, results);
    }

    private static boolean hasTraceErrors(Map<String, Object> invokeResult) {
        if (invokeResult == null) {
            return false;
        }
        Object traceObj = invokeResult.get("trace");
        if (traceObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> trace = (Map<String, Object>) traceObj;
            Object errors = trace.get("errorEvents");
            return errors instanceof List && !((List<?>) errors).isEmpty();
        }
        return false;
    }

    private static String extractErrorMessage(Map<String, Object> invokeResult) {
        if (invokeResult == null) {
            return "业务协议返回结果为空";
        }
        Object desc = invokeResult.get("errorDesc");
        if (desc != null && !String.valueOf(desc).isBlank()) {
            return String.valueOf(desc);
        }
        Object msg = invokeResult.get("message");
        if (msg != null && !String.valueOf(msg).isBlank()) {
            return String.valueOf(msg);
        }
        Object name = invokeResult.get("errorName");
        if (name != null && !String.valueOf(name).isBlank()) {
            return String.valueOf(name);
        }
        Object traceObj = invokeResult.get("trace");
        if (traceObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> trace = (Map<String, Object>) traceObj;
            Object errors = trace.get("errorEvents");
            if (errors instanceof List && !((List<?>) errors).isEmpty()) {
                return "服务端内部异常: " + ((List<?>) errors).get(0);
            }
        }
        return "业务协议返回非零错误码";
    }

    private static String detectDefectCategory(int errorCode, String message) {
        if (errorCode == ErrorMsgEnum.config_data_not_found.getId()) {
            return "[CONFIG_DEFECT]";
        }
        if (message != null) {
            String lower = message.toLowerCase();
            if (lower.contains("config") || lower.contains("decompose") || lower.contains("not implement") || lower.contains("配置")) {
                return "[CONFIG_DEFECT]";
            }
        }
        return "[CODE_DEFECT]";
    }

    private static String extractStackExcerpt(Throwable t) {
        if (t == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        String full = sw.toString();
        String[] lines = full.split("\r?\n");
        StringBuilder sb = new StringBuilder();
        int count = Math.min(lines.length, 5);
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append("\n");
            }
            sb.append(lines[i]);
        }
        return sb.toString();
    }
}
