package cn.game.games.net.game.module.develop.mergeequip;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.item.AbstractItemModule;
import cn.game.protocol.generated.config.EquipConfig;
import cn.game.protocol.generated.config.HCBattleConfig;
import cn.game.protocol.generated.manager.EquipManager;
import cn.game.protocol.generated.manager.HCBattleManager;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public class MergeEquipModule extends AbstractItemModule<MergeEquip> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.HCChapterFirstWin, EventTypeEnum.PLAYER_CREATE };

	/** 上阵的装备id */
	private List<Integer> equipList = new ArrayList<>();

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public MergeEquip add(int itemId, int count, OpType opType) {
		if (count <= 0) {
			return null;
		}
		EquipConfig equipConfig = EquipManager.instance().get(itemId);
		itemId = equipConfig.EquipGroup;
		MergeEquip item = id_items.get(itemId);
		if (item == null) {
			item = (MergeEquip) newInstance();
			setInstance(item, itemId, count);
			setInstanceAfter(item);
			item.insert();
			initAddCache(item);
		}
		return item;
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {
		case HCChapterFirstWin: {
			int battleId = event.getIntParameter(0);
			Collection<EquipConfig> list = EquipManager.instance().list();
			HCBattleConfig battleConfig = HCBattleManager.instance().get(battleId);

			for (EquipConfig equipConfig : list) {
				if (equipConfig.ChapterUnlock == battleConfig.Chapter) {
					if (equipConfig.ItemType == 1 || equipConfig.ItemType == 2) {
						MergeEquip mergeEquip = get(equipConfig.EquipGroup);
						if (mergeEquip == null) {
							add(equipConfig.ID, OpType.None);
						}
					}
				}
			}
			break; 
		} 
		case PLAYER_CREATE: {
			Collection<MergeEquip> list = list();
			// 初始装备全上阵
			for (MergeEquip item : list) {
				equipList.add(item.getConfigId());
			}
			break;
		}
		}
	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.Merge_Equip;
	}

	@Override
	public MergeEquip newInstance() {
		return new MergeEquip();
	}

	@Override
	public void setInstanceAfter(MergeEquip item) {
		item.setLevel(1);
	}

	@Override
	public RewardInfo toRewardInfo(MergeEquip item) {
		return RewardInfo.newBuilder().setMergeEquip(item.toMergeEquipProto()).build();
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		for (MergeEquip item : list()) {
			builder.addMergeEquips(item.toMergeEquipProto());
		}
		builder.addAllMergeEquipIds(equipList);
	}

	public List<Integer> getEquipList() {
		return equipList;
	}

	public void setEquipList(List<Integer> equipList) {
		this.equipList = equipList;
	}

	@Override
	public void checkConfig(int id) {
		EquipManager.instance().get(id);
	}

}
