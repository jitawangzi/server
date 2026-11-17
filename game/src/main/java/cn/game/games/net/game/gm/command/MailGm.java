package cn.game.games.net.game.gm.command;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.gm.AbstractGm;
import cn.game.games.net.game.gm.GmAction;
import cn.game.games.net.game.gm.GmGroup;
import cn.game.games.net.game.gm.GmParam;
import cn.game.games.net.game.gm.GmScope;
import cn.game.games.net.game.gm.GmSecurityTier;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.mail.MailModule;
import cn.game.games.net.game.module.mail.MailType;

/**
 * 邮件系统调试GM指令分组
 *
 * @author shuyongqiang
 */
@Component
@GmGroup(name = "mail", description = "邮件系统调试指令")
public class MailGm extends AbstractGm {

	@GmAction(name = "send", description = "给当前玩家发送测试邮件", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public void send(Player player,
			@GmParam(description = "邮件配置表ID(0为自定义邮件)", required = false, defaultValue = "0") int mailId,
			@GmParam(description = "邮件标题", required = false, defaultValue = "测试邮件") String title,
			@GmParam(description = "邮件内容", required = false, defaultValue = "这是一封自动化测试邮件") String content,
			@GmParam(description = "附加道具或资源ID(0表示无附件)", required = false, defaultValue = "0") int itemId,
			@GmParam(description = "附加道具数量", required = false, defaultValue = "0") int count) {
		List<Goods> attachmentList = null;
		if (itemId > 0 && count > 0) {
			attachmentList = new ArrayList<>();
			attachmentList.add(new Goods(itemId, count));
		}
		MailHelper.sendMail(player.getPlayerId(), mailId, null, "系统管理员", title, content,
				MailType.System.getValue(), attachmentList, true);
	}

	@GmAction(name = "clear", description = "清空当前玩家的所有已读邮件", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public void clear(Player player) {
		MailModule mailModule = player.getMailModule();
		if (mailModule != null) {
			mailModule.deleteAll();
		}
	}
}
