package cn.game.games.net.game.gm.command;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.gm.AbstractGm;
import cn.game.games.net.game.gm.GmAction;
import cn.game.games.net.game.gm.GmGroup;
import cn.game.games.net.game.gm.GmParam;
import cn.game.games.net.game.gm.GmScope;
import cn.game.games.net.game.gm.GmSecurityTier;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.protocol.generated.manager.QuestManager;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.RewardMsg.RewardPush_55000501;

/**
 * 任务系统调试GM指令分组
 *
 * @author shuyongqiang
 */
@Component
@GmGroup(name = "quest", description = "任务调试指令")
public class QuestGm extends AbstractGm {

	@GmAction(name = "quest", description = "完成指定任务并领奖", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public void quest(Player player,
			@GmParam(description = "任务配置ID", required = false, defaultValue = "0") int id) {
		QuestManager.instance().get(id);
		QuestModule module = player.getQuestModule();
		List<RewardInfo> items = module.finish(id, 0);
		PlayerHelper.sendProtocol(player.getPlayerId(), RewardPush_55000501.newBuilder().addAllRewards(items).build());
	}
}