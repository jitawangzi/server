package cn.game.games.diagnostics;

import java.nio.file.Path;
import java.util.Map;

/** 显式本地单订单 archive/verify CLI；不启动业务、不自动扫描或补发，stdout 只输出固定脱敏摘要。 */
public final class PaymentArchiveCli {
    /** 工具类不创建实例。 */
    private PaymentArchiveCli() {}

    /** @param args archive|verify playerId orderId absoluteOutputRoot absoluteExternalKey；失败返回非零进程码 */
    public static void main(String[] args) throws Exception {
        Map<String, Object> result = run(args);
        PaymentArchiveEvidence.JSON.writeValue(new java.io.FileOutputStream(java.io.FileDescriptor.out), result);
        if ("FAILED_RETAIN_SOURCE".equals(result.get("status"))) { System.exit(1); }
    }

    /** @param args 必须显式提供所有参数，不接受默认 key、批量范围或凭据内容 @return 脱敏回执 */
    public static Map<String, Object> run(String[] args) {
        try {
            PaymentArchiveEvidence.require(args.length == 5 && (args[0].equals("archive") || args[0].equals("verify"))
                    && args[1].matches("[1-9][0-9]{0,18}") && args[2].matches("[1-9][0-9]{0,18}"));
            long player = Long.parseLong(args[1]), order = Long.parseLong(args[2]);
            Path root = Path.of(args[3]), key = Path.of(args[4]);
            if (args[0].equals("verify")) { return PaymentArchiveStore.verify(root, key, player, order); }
            // 真实只读调用及连接清理退出后才封存归档，不以取消等待者当作采集终止。
            var content = PaymentArchiveEvidence.collect(player, order, new PaymentEvidenceCli.JdbcReader());
            return PaymentArchiveStore.seal(root, key, player, order, content, new PaymentArchiveStore.Disk() {});
        } catch (Exception failure) {
            PaymentArchiveStore.safeWarn(failure);
            if (failure instanceof InterruptedException) { Thread.currentThread().interrupt(); }
            return Map.of("status", "FAILED_RETAIN_SOURCE", "archiveIntegrity", "NOT_VERIFIED", "canDelete", false,
                    "canResend", false, "transactionResolution", "NOT_PROVEN", "platformAuthenticity", "NOT_PROVEN");
        }
    }
}
