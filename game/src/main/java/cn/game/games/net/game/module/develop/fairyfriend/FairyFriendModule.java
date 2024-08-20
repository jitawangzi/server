package cn.game.games.net.game.module.develop.fairyfriend;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.item.AbstractItemModule;
import cn.game.protocol.generated.config.FairyFriendConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.FairyFriendManager;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public class FairyFriendModule extends AbstractItemModule<FairyFriend> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.LevelUp, EventTypeEnum.FuncOpen };

	/** 仙友切磋记录 ，key: fight表id value：状态， 1以挑战，2已领奖 */
	private Map<Integer, Integer> fightMap = new HashMap<Integer, Integer>();
	
	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {
		case LevelUp: {
			int exp = event.getIntParameter(0);
			if (exp == Asset.playerExp.ID) {
				addFairyFriend();
			}
			break;
		}
		case FuncOpen: {
			InitialUI func = event.getParameter(0);
			if (func == InitialUI.FairyFriends) {
				addFairyFriend();
				// 初始化游历体力
				PlayerHelper.addResources(player, Asset.TravelStamina.ID, 1, OpType.FairyFriend, true);
				player.handleEvent(EventTypeEnum.ResourceAdd, Asset.TravelStamina.ID);
			}
			break;
		}
		}
	}

	private void addFairyFriend() {
		Collection<FairyFriendConfig> list = FairyFriendManager.instance().list(); 
		for (FairyFriendConfig fairyFriendConfig : list) {
			if (player.getLevel() >= fairyFriendConfig.Condition) {
				if (get(fairyFriendConfig.ID) == null) {
					add(fairyFriendConfig.ID, OpType.PlayerLevelUp) ; 
				}
			}
		}
	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.FairyFriend;
	}

	@Override
	public FairyFriend newInstance() {
		return new FairyFriend();
	}

	@Override
	public long genUid() {
		return 0;
	}
//	@Override
//	public void setInstanceAfter(FairyFriend item) {
//		item.setLevel(1);
//	}

	@Override
	public RewardInfo toRewardInfo(FairyFriend item) {
		// 仙友不会作为奖励
		return null;
	}

	public Map<Integer, Integer> getFightMap() {
		return fightMap;
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		for (FairyFriend item : list()) {
			builder.addItems(item.toItemInfo());
			builder.addFairyFriends(item.toProto());
		}
		builder.putAllFairyFriendFights(fightMap);
	}

	@Override
	public void checkConfig(int id) {
		FairyFriendManager.instance().get(id);
	}
}
