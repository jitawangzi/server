package cn.game.games.net.game.module.mail;

import java.util.ArrayList;
import java.util.List;
import cn.game.games.cache.entity.Mail;
import cn.game.games.net.game.module.award.Goods;
import cn.game.protocol.generated.config.MailConfig;
import cn.game.protocol.generated.manager.MailManager;
import cn.game.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 邮件查询与领取共用的纯资格判断；不访问奖励模块、不填充实体的懒解析缓存。 */
public final class MailRewards {
    /** 畸形持久数据只记录邮件标识，不输出附件原文。 */
    private static final Logger log = LoggerFactory.getLogger(MailRewards.class);
    /** 单封附件的解析上限，避免坏行消耗无界内存。 */
    private static final int MAX_ATTACHMENT_CHARS = 65536;

    private MailRewards() {}

    /**
     * 解析显式附件，保持原始实体不变。
     * @param mail 当前玩家的邮件
     * @return 空列表表示未指定或空附件；null表示畸形附件，不能回退到配表奖励
     */
    public static List<Goods> attachments(Mail mail) {
        String raw = mail.getAttachment();
        if (raw == null || raw.isBlank() || raw.trim().equals("null")) { return List.of(); }
        if (raw.length() > MAX_ATTACHMENT_CHARS) { return null; }
        try {
            var reader = JsonUtil.getObjectMapper().reader()
                    .with(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                    .with(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
            com.fasterxml.jackson.databind.JsonNode array = reader.readTree(raw);
            if (array == null || !array.isArray()) { return null; }
            List<Goods> result = new ArrayList<>();
            for (var item : array) {
                var id = item.get("id"); var count = item.get("count");
                if (id == null || count == null || !id.isIntegralNumber() || !count.isIntegralNumber()
                        || !id.canConvertToInt() || !count.canConvertToInt() || id.intValue() <= 0 || count.intValue() <= 0) {
                    return null;
                }
                result.add(new Goods(id.intValue(), count.intValue()));
            }
            return result;
        } catch (java.io.IOException failure) {
            log.warn("邮件附件解析失败，查询及领取忽略坏附件 playerId={} mailId={}", mail.getPlayerId(), mail.getId(), failure);
            return null;
        }
    }

    /**
     * 与领取入口保持一致，显式附件优先；空附件才使用 MailConfig.Reward。
     * @param mail 邮件实体
     * @param nowSeconds 当前秒时间戳，等于截止秒仍有效
     * @return 当前状态是否有可领取奖励；不会初始化玩家模块或执行任何写操作
     */
    public static boolean claimable(Mail mail, long nowSeconds) {
        if (mail.getIsDeleted() || mail.getReceive() || mail.getType() == 1 || mail.getCreateTime() > nowSeconds) { return false; }
        MailConfig config = MailManager.instance().getNullable(mail.getMailId());
        if (mail.getMailId() != 0 && config == null) { return false; }
        if (config != null && (config.Type == 1 || config.Expiration < 0
                || config.Expiration > 0 && nowSeconds > (long) mail.getCreateTime() + config.Expiration)) { return false; }
        List<Goods> explicit = attachments(mail);
        if (explicit == null) { return false; }
        if (!explicit.isEmpty()) { return true; }
        if (config == null || config.Reward == null || config.Reward.length == 0) { return false; }
        for (int[] reward : config.Reward) {
            if (reward == null || reward.length == 0 || reward.length % 2 != 0) { return false; }
            for (int value : reward) { if (value <= 0) { return false; } }
        }
        return true;
    }
}
