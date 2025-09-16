package cn.game.games.net.game.module.develop.gem;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.develop.equip.EquipModule;
import cn.game.games.net.game.module.develop.equip.EquipPart;
import cn.game.games.net.game.module.item.AbstractItemNoStackModule;
import cn.game.protocol.generated.config.GemAttrConfig;
import cn.game.protocol.generated.config.GemConfig;
import cn.game.protocol.generated.manager.GemAttrManager;
import cn.game.protocol.generated.manager.GemManager;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.util.Rnd;

public class GemModule extends AbstractItemNoStackModule<Gem> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(PlayerEvent event) {
		switch (event.getType()) {
		}
	}

	@Override
	public void setInstanceExt(Gem instance) {
		// 随机宝石属性
		GemConfig gemConfig = GemManager.instance().get(instance.getConfigId());
		List<GemAttrConfig> posqualityList = GemAttrManager.instance().getPosqualityList(gemConfig.pos, gemConfig.quality);
		GemAttrConfig config = Rnd.randomElement(posqualityList, r -> r.weight);
		instance.getEntryEffectList().add(config.effectId);
	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.Gem;
	}

	@Override
	public Gem newInstance() {
		return new Gem();
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		for (Gem obj : list()) {
			builder.addGems(obj.toGemInfo());
		}
	}

	@Override
	public void checkConfig(int id) {
		GemManager.instance().get(id);
	}

	public int getCountGTQualityWearCount(int quality) {
		EquipModule module = player.getModule(EquipModule.class); 
		Set<Entry<Integer, EquipPart>> entrySet = module.getEquipPartMap().entrySet(); 
		int ret = 0 ; 
		
		for (Entry<Integer, EquipPart> entry : entrySet) {
			Map<Long, Integer> gemPosMap = entry.getValue().getGemPosMap(); 
			for (Long gemUid : gemPosMap.keySet()) {
				Gem gem = get(gemUid);
				if (gem != null) {
					GemConfig config = GemManager.instance().get(gem.getConfigId());
					if (config.quality >= quality) {
						ret++;
					}
				}
			}
		}
		return ret; 
//		return (int) list().stream().filter(gem -> {
//			GemConfig config = GemManager.instance().get(gem.getConfigId());
//			return config.quality >= quality;
//		}).count();
	}
}
