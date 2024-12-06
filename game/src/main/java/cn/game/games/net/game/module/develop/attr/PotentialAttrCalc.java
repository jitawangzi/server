package cn.game.games.net.game.module.develop.attr;

import java.util.List;
import java.util.Map;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.module.develop.DevelopHelper;
import cn.game.games.net.game.module.develop.DevelopModule;
import cn.game.protocol.generated.config.PotentialConfig;
import cn.game.protocol.generated.config.RescueConfig;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.PotentialManager;
import cn.game.protocol.generated.manager.RescueManager;

public class PotentialAttrCalc extends PlayerAttrCalc {

	public PotentialAttrCalc(Player player) {
		super(player);
	}

	@Override
	public void calcAttr() {
		DevelopModule developModule = player.getDevelopModule();

		// 初始修炼等级
		// 突破属性： 当前值 = 初始值 +（lv-1）*每级成长值+INT(LV/10)*突破成长值
		Map<Integer, List<PotentialConfig>> potentialMarks = PotentialManager.instance().getPotentialMarks();
		potentialMarks.forEach((k, v) -> {
			int lv = developModule.getCultivationLv(k, 1);
			if (lv > 0) {
				PotentialConfig config = DevelopHelper.getPotentialConfig(v, lv);
				int attrValue = config.PotentialBase[1] + (lv - 1) * config.PotentialGrow[1] + (lv / 10) * config.BreakthroughGrowth[1];
				attrMap.add(config.PotentialBase[0], attrValue);
			}
		});

		Map<Integer, List<RescueConfig>> rescueMarks = RescueManager.instance().getRescueMarks();
		rescueMarks.forEach((k, v) -> {
			int lv = developModule.getCultivationLv(k, 2);
			if (lv > 0) {
				RescueConfig config = DevelopHelper.getRescueConfig(v, lv);
				attrMap.add(config.RescueMulHurtPerGrow[0], config.RescueMulHurtPerGrow[1] * lv);
			}
		});
	}
	@Override
	public InitialUI getFunction() {
		return InitialUI.Consciousness;
	}

	@Override
	public AttrCalcType getAttrCalcType() {
		return AttrCalcType.Potential;
	}
}
