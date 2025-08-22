package cn.game.games.net.game.module.develop.attr;

import java.util.Map;

import cn.game.games.cache.entity.Equip;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.module.develop.equip.EquipModule;
import cn.game.games.net.game.module.develop.equip.EquipPart;
import cn.game.protocol.generated.config.EquipConfig;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.EquipManager;

public class EquipAttrCalc extends PlayerAttrCalc {

	public EquipAttrCalc(Player player) {
		super(player);
	}

	@Override
	public void calcAttr() {
		EquipModule equipModule = player.getModule(EquipModule.class);
		Map<Integer, EquipPart> equipPartMap = equipModule.getEquipPartMap();
		equipPartMap.forEach((k, v) -> {
			Equip equip = equipModule.get(v.getEquipUid());
			EquipConfig equipConfig = EquipManager.instance().get(equip.getConfigId());

			// 基础属性 + 强化属性
			attrMap.add(equipConfig.baseAttrId, equipConfig.baseAttrValue + (v.getStrength() - 1) * equipConfig.stepSize); 
			
			for (int i = 0; i < equipConfig.strengthenLevels.length; i++) {
				if (v.getStrength() >= equipConfig.strengthenLevels[i]) {
					attrMap.add(equipConfig.strengthenIds[i], equipConfig.strengthenValuess[i]);
				} else {
					break;
				}
			}
		});
	}

	@Override
	public InitialUI getFunction() {
		return null;
	}

	@Override
	public AttrCalcType getAttrCalcType() {
		return AttrCalcType.Equip;
	}
}
