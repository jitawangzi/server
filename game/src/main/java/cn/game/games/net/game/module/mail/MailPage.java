package cn.game.games.net.game.module.mail;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import cn.game.core.exception.LogicException;
import cn.game.games.cache.entity.Mail;
import cn.game.protocol.manual.ErrorMsgEnum;

/**
 * 玩家邮箱内的有界选择：按创建秒、唯一ID倒序，保留至多 pageSize+1 个候选。
 * 游标签名绑定玩家、筛选和起始上界，进程重启后失效；不保存跨请求快照或承诺变更期间不漏项。
 */
public final class MailPage {
    /** 新客户端未指定页大小时的有限默认值。 */
    public static final int DEFAULT_SIZE = 50;
    /** 每页最大返回数量。 */
    public static final int MAX_SIZE = 100;
    /** 37字节载荷和32字节HMAC的无填充Base64长度。 */
    private static final int CURSOR_LENGTH = 92;
    /** 固定排序避免同秒邮件在页边界重复。 */
    private static final Comparator<Mail> ORDER = Comparator.comparingInt(Mail::getCreateTime)
            .thenComparingLong(Mail::getId).reversed();
    /** 仅用于游标完整性，重启后要求客户端重新开始；不进入持久化或配置。 */
    private static final SecretKey KEY = newKey();

    private MailPage() {}

    /** @param mails 本页有序邮件 @param nextCursor 空字符串表示本次扫描到尾 */
    public record Result(List<Mail> mails, String nextCursor) {}

    /**
     * 只在已鉴权玩家所属队列调用，单次扫描 O(n log pageSize)，不额外加载其他玩家或模块。
     * @param source 当前邮箱
     * @param playerId 权威连接玩家ID
     * @param filter 0全部、1未读、2可领取
     * @param size 已解析的页大小
     * @param cursor 原样传回的上页游标
     * @param nowSeconds 本次统一时间
     * @return 有限结果和后续游标
     */
    public static Result select(Collection<Mail> source, long playerId, int filter, int size, String cursor, long nowSeconds) {
        if (filter < 0 || filter > 2) { throw invalid("MAIL_FILTER_UNKNOWN"); }
        if (size < 1 || size > MAX_SIZE) { throw invalid("MAIL_PAGE_SIZE_INVALID"); }
        Boundary bound = cursor.isEmpty() ? null : decode(cursor, playerId, filter);
        PriorityQueue<Mail> selected = new PriorityQueue<>(size + 2, ORDER.reversed());
        for (Mail mail : source) {
            if (mail.getPlayerId() != playerId || mail.getIsDeleted()) { continue; }
            if (bound != null && (compare(mail, bound.upperTime, bound.upperId) > 0
                    || compare(mail, bound.lastTime, bound.lastId) >= 0)) { continue; }
            if (filter == 1 && mail.getSee() || filter == 2 && !MailRewards.claimable(mail, nowSeconds)) { continue; }
            selected.add(mail);
            if (selected.size() > size + 1) { selected.poll(); }
        }
        List<Mail> rows = new ArrayList<>(selected); rows.sort(ORDER);
        boolean more = rows.size() > size;
        if (more) { rows.remove(rows.size() - 1); }
        String next = "";
        if (more) {
            Mail first = rows.getFirst(); Mail last = rows.getLast();
            next = encode(new Boundary(playerId, filter, bound == null ? first.getCreateTime() : bound.upperTime,
                    bound == null ? first.getId() : bound.upperId, last.getCreateTime(), last.getId()));
        }
        return new Result(List.copyOf(rows), next);
    }

    /** @param mail 候选 @param time 边界秒 @param id 边界ID @return 自然升序比较值 */
    private static int compare(Mail mail, int time, long id) {
        int result = Integer.compare(mail.getCreateTime(), time);
        return result == 0 ? Long.compare(mail.getId(), id) : result;
    }

    /** @param playerId 归属 @param filter 筛选 @param upperTime 起点秒 @param upperId 起点ID @param lastTime 末项秒 @param lastId 末项ID */
    private record Boundary(long playerId, int filter, int upperTime, long upperId, int lastTime, long lastId) {}

    /** @return 进程内加密随机密钥；生成失败阻止初始化，不能退化为无签名游标 */
    private static SecretKey newKey() {
        try { KeyGenerator generator = KeyGenerator.getInstance("HmacSHA256"); return generator.generateKey(); }
        catch (GeneralSecurityException failure) {
            org.slf4j.LoggerFactory.getLogger(MailPage.class).error("邮件游标签名密钥初始化失败", failure);
            throw new IllegalStateException("MAIL_CURSOR_KEY_UNAVAILABLE", failure);
        }
    }

    /** @param bytes 游标载荷 @return HMAC签名 */
    private static byte[] sign(byte[] bytes) {
        try { Mac mac = Mac.getInstance("HmacSHA256"); mac.init(KEY); return mac.doFinal(bytes); }
        catch (GeneralSecurityException failure) {
            org.slf4j.LoggerFactory.getLogger(MailPage.class).error("邮件游标签名失败", failure);
            throw new IllegalStateException("MAIL_CURSOR_SIGNATURE_UNAVAILABLE", failure);
        }
    }

    /** @param bound 本页边界 @return 签名后的不透明游标 */
    private static String encode(Boundary bound) {
        byte[] payload = ByteBuffer.allocate(37).put((byte) 1).putLong(bound.playerId).putInt(bound.filter)
                .putInt(bound.upperTime).putLong(bound.upperId).putInt(bound.lastTime).putLong(bound.lastId).array();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(ByteBuffer.allocate(69).put(payload).put(sign(payload)).array());
    }

    /** @param text 客户端游标 @param playerId 权威玩家 @param filter 本次筛选 @return 验证后的边界 */
    private static Boundary decode(String text, long playerId, int filter) {
        if (text.length() != CURSOR_LENGTH || !text.matches("[A-Za-z0-9_-]{92}")) { throw invalid("MAIL_CURSOR_MALFORMED"); }
        byte[] bytes = Base64.getUrlDecoder().decode(text);
        byte[] payload = java.util.Arrays.copyOf(bytes, 37);
        if (!MessageDigest.isEqual(sign(payload), java.util.Arrays.copyOfRange(bytes, 37, 69))) { throw invalid("MAIL_CURSOR_INVALID"); }
        ByteBuffer data = ByteBuffer.wrap(payload);
        if (data.get() != 1) { throw invalid("MAIL_CURSOR_VERSION"); }
        Boundary result = new Boundary(data.getLong(), data.getInt(), data.getInt(), data.getLong(), data.getInt(), data.getLong());
        if (result.playerId != playerId || result.filter != filter) { throw invalid("MAIL_CURSOR_SCOPE_MISMATCH"); }
        return result;
    }

    /** @param reason 可辨识的参数错误原因 @return 既有错误码异常 */
    private static LogicException invalid(String reason) {
        return new LogicException(ErrorMsgEnum.request_parameter_error.getId(), reason);
    }
}
