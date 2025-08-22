package cn.game.games.net.game.module.develop.attr;

import cn.game.games.cache.entity.Player;
import cn.game.protocol.generated.config.HeavenlyDaoConfig;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.HeavenlyDaoManager;

public class HeavenDaoAttrCalc extends PlayerAttrCalc {
	public HeavenDaoAttrCalc(Player player) {
		super(player);
	}
	@Override
	public void calcAttr() {

		/*		int heavenlyDaoLevel = player.getDevelopModule().getHeavenlyDaoLevel();
				if (heavenlyDaoLevel == 0) {
					return;
				}
				HeavenlyDaoConfig heavenlyDaoConfig = HeavenlyDaoManager.instance().get(heavenlyDaoLevel);
				for (int[] att : heavenlyDaoConfig.Attribute) {
					attrMap.add(att);
				}*/
	}

	@Override
	public InitialUI getFunction() {
		return InitialUI.HeavenlyDaoCultivation;
	}

	@Override
	public AttrCalcType getAttrCalcType() {
		return AttrCalcType.HeavenlyDao;
	}
}
