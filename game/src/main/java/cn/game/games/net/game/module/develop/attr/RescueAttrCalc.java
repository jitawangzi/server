package cn.game.games.net.game.module.develop.attr;

import java.util.List;
import java.util.Map;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.module.develop.DevelopHelper;
import cn.game.games.net.game.module.develop.DevelopModule;
import cn.game.protocol.generated.config.RescueConfig;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.RescueManager;

/**    
 * 强援属性
 * 2024年12月20日 18:00:40
 * @author SYQ
 */
public class RescueAttrCalc extends PlayerAttrCalc {

	public RescueAttrCalc(Player player) {
		super(player);
	}

	@Override
	public void calcAttr() {
		DevelopModule developModule = player.getDevelopModule();
		// 强援
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
		return InitialUI.HuDaoQiangYuan;
	}

	@Override
	public AttrCalcType getAttrCalcType() {
		return AttrCalcType.Rescue;
	}
}
