package cn.game.games.net.game.module.develop.attr;

import java.util.Map;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.module.develop.equip.EquipModule;
import cn.game.games.net.game.module.develop.equip.EquipPart;
import cn.game.games.net.game.module.develop.gem.Gem;
import cn.game.games.net.game.module.develop.gem.GemModule;
import cn.game.protocol.generated.config.EntryEffectConfig;
import cn.game.protocol.generated.enume.EntryEffectEnum;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.EntryEffectManager;

public class GemAttrCalc extends PlayerAttrCalc {

	public GemAttrCalc(Player player) {
		super(player);
	}
	@Override
	public void calcAttr() {
		GemModule gemModule = player.getModule(GemModule.class);
		EquipModule equipModule = player.getModule(EquipModule.class);
		Map<Integer, EquipPart> equipPartMap = equipModule.getEquipPartMap(); 
		equipPartMap.forEach((k, v) -> {
			Map<Long, Integer> gemPosMap = v.getGemPosMap(); 
			gemPosMap.forEach((uid, pos) -> {
				Gem gem = gemModule.get(uid); 
				for (Integer attr : gem.getAttrList()) {
					EntryEffectConfig entryEffectConfig = EntryEffectManager.instance().get(attr); 
					if (entryEffectConfig.type == EntryEffectEnum.AttributeChange.ID) {
						attrMap.add(entryEffectConfig.idParam, entryEffectConfig.numParam);
					}
				}
			});
		}); 
	}

	@Override
	public InitialUI getFunction() {
		return null;
	}

	@Override
	public AttrCalcType getAttrCalcType() {
		return AttrCalcType.Gem;
	}
}
