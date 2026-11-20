package cn.game.games.net.game.gm.acceptance;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 模块验收单条用例执行结果载荷
 *
 * @author shuyongqiang
 */
public class AcceptanceCaseResult {

    /** 用例唯一标识符 */
    private final String caseId;
    /** 用例标题或简述 */
    private final String title;
    /** 验收运行模式 */
    private final AcceptanceRunMode mode;
    /** 用例终态 */
    private final AcceptanceCaseStatus status;
    /** 根本原因分类（如 [CONFIG_DEFECT] / [CODE_DEFECT]） */
    private final String defectCategory;
    /** 关联的配置表名 */
    private final String configTable;
    /** 关联的配置行ID */
    private final int configId;
    /** 命中的多态分支键 */
    private final String branchKey;
    /** 业务返回错误码 */
    private final int errorCode;
    /** 错误码名称 */
    private final String errorName;
    /** 错误详细信息 */
    private final String message;
    /** 异常堆栈摘录 */
    private final String stackExcerpt;
    /** 单条耗时（毫秒） */
    private final long elapsedMs;

    public AcceptanceCaseResult(String caseId, String title, AcceptanceRunMode mode,
            AcceptanceCaseStatus status, String defectCategory, String configTable,
            int configId, String branchKey, int errorCode, String errorName,
            String message, String stackExcerpt, long elapsedMs) {
        this.caseId = caseId == null ? "" : caseId;
        this.title = title == null ? "" : title;
        this.mode = mode == null ? AcceptanceRunMode.ALL : mode;
        this.status = status == null ? AcceptanceCaseStatus.PASSED : status;
        this.defectCategory = defectCategory == null ? "" : defectCategory;
        this.configTable = configTable == null ? "" : configTable;
        this.configId = configId;
        this.branchKey = branchKey == null ? "" : branchKey;
        this.errorCode = errorCode;
        this.errorName = errorName == null ? "" : errorName;
        this.message = message == null ? "" : message;
        this.stackExcerpt = stackExcerpt == null ? "" : stackExcerpt;
        this.elapsedMs = elapsedMs;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getTitle() {
        return title;
    }

    public AcceptanceRunMode getMode() {
        return mode;
    }

    public AcceptanceCaseStatus getStatus() {
        return status;
    }

    public String getDefectCategory() {
        return defectCategory;
    }

    public String getConfigTable() {
        return configTable;
    }

    public int getConfigId() {
        return configId;
    }

    public String getBranchKey() {
        return branchKey;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorName() {
        return errorName;
    }

    public String getMessage() {
        return message;
    }

    public String getStackExcerpt() {
        return stackExcerpt;
    }

    public long getElapsedMs() {
        return elapsedMs;
    }

    /**
     * 将结果转换为标准 Map 结构，供 HTTP JSON 与工作台渲染
     *
     * @return 结构化结果 Map
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("caseId", caseId);
        map.put("title", title);
        map.put("mode", mode.name());
        map.put("status", status.name());
        map.put("defectCategory", defectCategory);
        map.put("configTable", configTable);
        map.put("configId", configId);
        map.put("branchKey", branchKey);
        map.put("errorCode", errorCode);
        map.put("errorName", errorName);
        map.put("message", message);
        map.put("stackExcerpt", stackExcerpt);
        map.put("elapsedMs", elapsedMs);
        return map;
    }
}
