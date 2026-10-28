package cn.game.games.diagnostics;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import cn.game.games.net.game.helper.PaymentDedupSchema;
import cn.game.games.net.game.helper.PaymentDedupStore;

/**
 * 有界、只读的支付启用结构预检。复用发货前置契约，报告只表示本次元数据观察。
 * 不执行DDL、不改开关、不认证外部部署前提，也不读取玩家或订单正文。
 */
public final class PaymentEnablementCli {
    /** schema检查无法证明的外部前提，必须由运维独立取得证据。 */
    private static final List<String> EXTERNAL = List.of("PLATFORM_GLOBAL_SDK_NAMESPACE_NOT_PROVEN",
            "OLD_WRITERS_AND_UNKNOWN_NOT_FENCED", "LEGACY_RECEIPT_COVERAGE_NOT_PROVEN");

    /** 测试只注入只读结构观察，不接受表名或业务动作参数。 */
    public interface Reader {
        /** @return 与生产发货相同的结构报告 @throws Exception 元数据或配置不可读 */
        PaymentDedupSchema.Report inspect() throws Exception;
    }

    /** 工具类不创建实例。 */
    private PaymentEnablementCli() {}

    /** @param args 不接受参数；S2_DB_CONFIG指向运维提供的私有本轮配置 @throws Exception 输出失败 */
    public static void main(String[] args) throws Exception {
        var result = run(args);
        PaymentArchiveEvidence.JSON.writeValue(new java.io.FileOutputStream(java.io.FileDescriptor.out), result);
        System.exit(exitCode(result));
    }

    /** @param args 空数组 @return 脱敏报告，所有结果均无启用/清理/重放授权 */
    public static Map<String, Object> run(String[] args) {
        if (args == null || args.length != 0) { return report(null, List.of("NO_ARGUMENTS_ALLOWED"), "INVALID_INPUT"); }
        try {
            var reader = new PaymentEvidenceCli.JdbcReader();
            try (var c = reader.connect("")) {
                var schema = PaymentDedupStore.inspectSchema(c);
                return report(schema, schema.blockers(), schema.valid() ? "BLOCKED_EXTERNAL_PREREQUISITES" : "BLOCKED_SCHEMA");
            }
        } catch (Exception failure) {
            PaymentArchiveStore.safeWarn(failure);
            return report(null, List.of("SCHEMA_CONFIG_OR_METADATA_UNAVAILABLE"), "BLOCKED_SCHEMA");
        }
    }

    /** @param args 空数组 @param reader 只读观察依赖 @return 总等待不超过12秒的报告 */
    public static Map<String, Object> run(String[] args, Reader reader) { return run(args, reader, 12000); }

    /** @param args 空数组 @param reader 只读边界 @param waitMillis 包内测试可缩短的等待毫秒 @return 有界报告 */
    static Map<String, Object> run(String[] args, Reader reader, long waitMillis) {
        if (args == null || args.length != 0) { return report(null, List.of("NO_ARGUMENTS_ALLOWED"), "INVALID_INPUT"); }
        var executor = Executors.newSingleThreadExecutor(Thread.ofPlatform().daemon().factory());
        try {
            var future = executor.submit(reader::inspect);
            try {
                var schema = future.get(waitMillis, TimeUnit.MILLISECONDS);
                return report(schema, schema.blockers(), schema.valid() ? "BLOCKED_EXTERNAL_PREREQUISITES" : "BLOCKED_SCHEMA");
            } finally { executor.shutdown(); }
        } catch (Exception failure) {
            PaymentArchiveStore.safeWarn(failure);
            if (failure instanceof InterruptedException) { Thread.currentThread().interrupt(); }
            var result = report(null, List.of(failure instanceof java.util.concurrent.TimeoutException ? "SCHEMA_OBSERVATION_TIMEOUT"
                    : "SCHEMA_CONFIG_OR_METADATA_UNAVAILABLE"), "BLOCKED_SCHEMA");
            result.put("readerExited", executor.isTerminated());
            return result;
        } finally { executor.shutdown(); }
    }

    /** @param schema 可选局部观察 @param blockers 固定原因 @param status 固定状态 @return 不含原始元数据的报告 */
    private static Map<String, Object> report(PaymentDedupSchema.Report schema, List<String> blockers, String status) {
        var out = new LinkedHashMap<String, Object>();
        out.put("contractVersion", PaymentDedupSchema.CONTRACT_VERSION); out.put("schemaSignature", schema == null ? null : schema.signature());
        out.put("status", status); out.put("localSchemaReady", schema != null && schema.valid()); out.put("localBlockers", blockers);
        out.put("externalBlockers", EXTERNAL); out.put("observation", "NON_ATOMIC_METADATA_NO_DDL_FENCE");
        out.put("nextAction", "COMPARE_V1_MIGRATION_AND_OBTAIN_EXTERNAL_DEPLOYMENT_PROOFS");
        out.put("canEnable", false); out.put("canDelete", false); out.put("canResend", false); return out;
    }

    /** @param report 当前观察报告 @return 1非法输入、2局部结构就绪但缺外部证明、3结构或读取阻断；无成功启用码 */
    public static int exitCode(Map<String, Object> report) {
        return "INVALID_INPUT".equals(report.get("status")) ? 1 : Boolean.TRUE.equals(report.get("localSchemaReady")) ? 2 : 3;
    }
}
