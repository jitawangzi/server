package cn.game.games.net.game.gm.acceptance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 模块验收汇总报告对象
 *
 * @author shuyongqiang
 */
public class ModuleAcceptanceReport {

    private final int moduleId;
    private final String moduleKey;
    private final String title;
    private final AcceptanceRunMode mode;
    private final int totalCases;
    private final int passedCases;
    private final int failedCases;
    private final int provisionFailures;
    private final int fixtureDefects;
    private final int sutFailures;
    private final int assertionFailures;
    private final long elapsedMs;
    private final List<Map<String, Object>> branchCoverage;
    private final List<Map<String, Object>> failedItems;
    private final List<String> warnings;
    private final List<AcceptanceCaseResult> results;

    public ModuleAcceptanceReport(int moduleId, String moduleKey, String title,
            AcceptanceRunMode mode, int totalCases, int passedCases, int failedCases,
            int provisionFailures, int fixtureDefects, int sutFailures, int assertionFailures,
            long elapsedMs, List<Map<String, Object>> branchCoverage,
            List<Map<String, Object>> failedItems, List<String> warnings) {
        this(moduleId, moduleKey, title, mode, totalCases, passedCases, failedCases,
                provisionFailures, fixtureDefects, sutFailures, assertionFailures, elapsedMs,
                branchCoverage, failedItems, warnings, Collections.emptyList());
    }

    public ModuleAcceptanceReport(int moduleId, String moduleKey, String title,
            AcceptanceRunMode mode, int totalCases, int passedCases, int failedCases,
            int provisionFailures, int fixtureDefects, int sutFailures, int assertionFailures,
            long elapsedMs, List<Map<String, Object>> branchCoverage,
            List<Map<String, Object>> failedItems, List<String> warnings,
            List<AcceptanceCaseResult> results) {
        this.moduleId = moduleId;
        this.moduleKey = moduleKey == null ? "" : moduleKey;
        this.title = title == null ? "" : title;
        this.mode = mode == null ? AcceptanceRunMode.ALL : mode;
        this.totalCases = totalCases;
        this.passedCases = passedCases;
        this.failedCases = failedCases;
        this.provisionFailures = provisionFailures;
        this.fixtureDefects = fixtureDefects;
        this.sutFailures = sutFailures;
        this.assertionFailures = assertionFailures;
        this.elapsedMs = elapsedMs;
        this.branchCoverage = branchCoverage == null ? new ArrayList<>() : new ArrayList<>(branchCoverage);
        this.failedItems = failedItems == null ? new ArrayList<>() : new ArrayList<>(failedItems);
        this.warnings = warnings == null ? new ArrayList<>() : new ArrayList<>(warnings);
        this.results = results == null ? new ArrayList<>() : new ArrayList<>(results);
    }

    public int getModuleId() {
        return moduleId;
    }

    public String getModuleKey() {
        return moduleKey;
    }

    public String getTitle() {
        return title;
    }

    public AcceptanceRunMode getMode() {
        return mode;
    }

    public int getTotalCases() {
        return totalCases;
    }

    public int getPassedCases() {
        return passedCases;
    }

    public int getFailedCases() {
        return failedCases;
    }

    public int getProvisionFailures() {
        return provisionFailures;
    }

    public int getFixtureDefects() {
        return fixtureDefects;
    }

    public int getSutFailures() {
        return sutFailures;
    }

    public int getAssertionFailures() {
        return assertionFailures;
    }

    public long getElapsedMs() {
        return elapsedMs;
    }

    public List<Map<String, Object>> getBranchCoverage() {
        return branchCoverage;
    }

    public List<Map<String, Object>> getFailedItems() {
        return failedItems;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public List<AcceptanceCaseResult> getResults() {
        return results;
    }

    /**
     * 判断本轮验收是否完全成功（用例数大于0且无失败用例）
     *
     * @return true表示全部通过
     */
    public boolean isSuccess() {
        return totalCases > 0 && failedCases == 0;
    }

    /**
     * 输出结构化报告 Map，保证所有集合均为 List 结构，不暴露未定义裸 Map
     *
     * @return 结构化报告字典
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("success", isSuccess());
        map.put("moduleId", moduleId);
        map.put("moduleKey", moduleKey);
        map.put("title", title);
        map.put("mode", mode.name());
        map.put("totalCases", totalCases);
        map.put("passedCases", passedCases);
        map.put("failedCases", failedCases);
        map.put("elapsedMs", elapsedMs);

        Map<String, Object> statusCounts = new LinkedHashMap<>();
        statusCounts.put("passed", passedCases);
        statusCounts.put("provisionFailed", provisionFailures);
        statusCounts.put("fixtureDefect", fixtureDefects);
        statusCounts.put("sutFailure", sutFailures);
        statusCounts.put("assertionFailed", assertionFailures);
        map.put("statusCounts", statusCounts);

        map.put("branchCoverage", branchCoverage);
        map.put("failedItems", failedItems);
        map.put("warnings", warnings);
        return map;
    }
}
