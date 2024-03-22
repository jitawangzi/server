package cn.game.games.net.game.module.buff;

import cn.game.games.cache.entity.Buff;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.op.impl.BuffOp;
import cn.game.games.core.event.EventHandler;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.OldGlobalConst;
import cn.game.protocol.generated.enume.EffectEnum;
import cn.game.protocol.generated.enume.EffectTargetTypeEnum;
import cn.game.util.Rnd;
/**
 * buff使用 事件处理器
 */
public class BuffUseEventHandler implements EventHandler {

	private Buff buff;

	public BuffUseEventHandler(Buff buff) {
		this.buff = buff;
	}
	public BuffUseEventHandler() {
	}

	@Override
	public void handleEvent(GameEvent event) {
		long playerId = buff.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		BuffOp buffOp = player.getModule(BuffOp.class);
		int[] params = {};
		if (event.getType() == EventTypeEnum.ExploreRoleResurrection) {
			int roleUid = event.getIntParameter(0);
		} else if (event.getType() == EventTypeEnum.ExploreNpcRound) {
			if (buff.getTarget() != event.getLongParameter(0)) {
				return;
			}
		} else if (event.getType() == EventTypeEnum.BattleEnd) {
			int lineupId = event.getIntParameter(0);
			// 不是探索战斗的忽略
			if (lineupId != OldGlobalConst.exploreTeamId && lineupId != OldGlobalConst.exploreTeamId) {
				return;
			}
		} else if(event.getType() == EventTypeEnum.ExploreWoundedInBattle) {
			int roleUid = event.getIntParameter(0);
			int attId = event.getIntParameter(1);
			int lossHp = event.getIntParameter(2);
			if (buff.getTarget() != roleUid) {
				return;
			}
			// 没有损失hp
			if (lossHp <= 0) {
				return;
			}
			params = new int[] { attId, lossHp };
			
		} else if(event.getType() == EventTypeEnum.ExploreBoxAndRemainsReward) {
			// 是否有奖励翻倍的效果
			boolean have = buffOp.hasEffectType(EffectTargetTypeEnum.Explore, playerId, EffectEnum.ExplorationSpirit);
			if (have) {
				BuffValue buffValue = buffOp.getBuffValue(EffectTargetTypeEnum.Explore, playerId, EffectEnum.ExplorationSpirit);
				// 翻倍概率
				int percent = buffValue.getValueDefault();
				if (!Rnd.hitPercentage(percent)) {
					return;
				}
			}
			
		} else if(event.getType() == EventTypeEnum.ExploreMaterialReward) {
			// 暴力开采有结晶翻倍的效果
			boolean have = buffOp.hasEffectType(EffectTargetTypeEnum.Explore, playerId, EffectEnum.ViolenceMining);
			if (have) {
				BuffValue buffValue = buffOp.getBuffValue(EffectTargetTypeEnum.Explore, playerId, EffectEnum.ViolenceMining);
				// 翻倍概率
				int percent = buffValue.getValueDefault();
				if (!Rnd.hitPercentage(percent)) {
					return;
				}
			}
		}

		buff.continuousEffect(params);
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
