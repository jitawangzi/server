package cn.game.games.net.game.module.develop.equip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.game.games.cache.entity.Equip;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.data.mapper.EquipMapper;
import cn.game.games.net.game.module.item.AbstractItemNoStackModule;
import cn.game.protocol.generated.config.EquipAttrConfig;
import cn.game.protocol.generated.config.EquipConfig;
import cn.game.protocol.generated.manager.EquipAttrManager;
import cn.game.protocol.generated.manager.EquipManager;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.protobuf.BaseMsg.EquipPartInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.util.Rnd;
/**    
 * 装备模块
 * 2024年2月19日 上午10:55:53
 * @author SYQ
 */
public class EquipModule extends AbstractItemNoStackModule<Equip> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE };

	/** 部位数据，  */
	private Map<Integer, EquipPart> equipPartMap = new HashMap<Integer, EquipPart>(); // 部位类型，1-6

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(PlayerEvent event) {
		switch (event.getType()) {
			case PLAYER_CREATE: {
				for (int pos = 1; pos < 7; pos++) {
					equipPartMap.put(pos, new EquipPart(pos));
				}
				for (Equip equip : list()) {
					EquipConfig equipConfig = EquipManager.instance().get(equip.getConfigId());
					EquipPart equipPart = getEquipPart(equipConfig.pos);
					equipPart.setEquipUid(equip.getId());
				}
				break;
			}
		}
	}

	@Override
	public Class<?>[] defaultDbMapperClass() {
		return new Class<?>[] { EquipMapper.class };
	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.Equipment;
	}

	@Override
	public Equip newInstance() {
		return new Equip();
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		for (Equip equip : list()) {
            builder.addEquips(equip.toEquipInfo());
        }
		for (EquipPart equipPart : equipPartMap.values()) {
			EquipPartInfo equipPartInfo = equipPart.toEquipPartInfo();
			builder.addEquipParts(equipPartInfo);
		}
	}
	
	@Override
	public void setInstanceExt(Equip equip) {
		equip.setLevel(1);
		EquipConfig equipConfig = EquipManager.instance().get(equip.getConfigId());
		int count = Rnd.get(equipConfig.attrCountMin, equipConfig.attrCountMax);
		List<EquipAttrConfig> list = EquipAttrManager.instance().list();
		List<EquipAttrConfig> filterList = new ArrayList<>();
		for (EquipAttrConfig equipAttrConfig : list) {
			if (equipConfig.level >= equipAttrConfig.levelMin && equipConfig.level <= equipAttrConfig.levelMax
					&& equipConfig.quality >= equipAttrConfig.qualityMin && equipConfig.quality <= equipAttrConfig.qualityMax) {
				filterList.add(equipAttrConfig);
			}
		}
		for (int i = 0; i < count; i++) {
			EquipAttrConfig equipAttrConfig = Rnd.randomElement(filterList);
			equip.getEquipAttrs().put(equipAttrConfig.ID, Rnd.get(equipAttrConfig.attrMin, equipAttrConfig.attrMax));
		}

	}

	@Override
	public void checkConfig(int id) {
		EquipManager.instance().get(id);
	}

	public EquipPart getEquipPart(int pos) {
		if (pos <= 0 || pos > 6) {
			throw new IllegalArgumentException("装备部位类型错误: " + pos);
		}
		return equipPartMap.computeIfAbsent(pos, v -> new EquipPart(pos));
	}

}
