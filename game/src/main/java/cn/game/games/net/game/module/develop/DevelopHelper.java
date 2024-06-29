package cn.game.games.net.game.module.develop;

import java.util.List;

import cn.game.protocol.generated.config.PotentialConfig;
import cn.game.protocol.generated.config.RescueConfig;
import cn.game.protocol.generated.manager.PotentialManager;
import cn.game.protocol.generated.manager.RescueManager;
import cn.game.util.BinarySearchUtil;

public class DevelopHelper {

	/** 
	 * 根据当前的修炼等级，获取对应修炼配置
	 * @param list
	 * @param level
	 * @return
	 */
	public static PotentialConfig getPotentialConfig(List<PotentialConfig> list, int level) {
		return BinarySearchUtil.findFirstGreaterThan(list, level, r -> r.compareValue());
	}

	/** 
	 * 根据当前的修炼等级，获取对应修炼配置
	 * @param type
	 * @param level
	 * @return
	 */
	public static PotentialConfig getPotentialConfig(int type, int level) {
		List<PotentialConfig> potentialMarkList = PotentialManager.instance().getPotentialMarkList(type);
		return getPotentialConfig(potentialMarkList, level);
	}

	/** 
	 * 根据当前的强援等级，获取对应强援配置
	 * @param list
	 * @param level
	 * @return
	 */
	public static RescueConfig getRescueConfig(List<RescueConfig> list, int level) {
		for (RescueConfig rescueConfig : list) {
			if (level <= rescueConfig.RescueLv) {
				return rescueConfig;
			}
		}
		return null;
	}

	/** 
	 * 根据当前的强援等级，获取对应强援配置
	 * @param type
	 * @param level
	 * @return
	 */
	public static RescueConfig getRescueConfig(int type, int level) {
		List<RescueConfig> list = RescueManager.instance().getRescueMarkList(type);
		return getRescueConfig(list, level);
	}

	/** 
	 * 计算修炼升级消耗的资源数量
	 * @param init
	 * @param a
	 * @param b
	 * @param currentLevel
	 * @return
	 */
	public static int calcPotentialConsumeValue(int init, int a, int b, int currentLevel) {
		// 实现公式：当前值=INT((固定系数a*(当前等级+固定系数b)^2+初始值)/100+1)*10
		double calculation = a * Math.pow(currentLevel + b, 2) + init;
		calculation = calculation / 100 + 1;
		int result = (int) calculation * 10;
		return result;
	}
}
