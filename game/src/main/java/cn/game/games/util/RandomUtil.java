package cn.game.games.util;

import java.util.ArrayList;
import java.util.List;

import cn.game.protocol.generated.config.RandomRewardConfig;
import cn.game.protocol.generated.config.RandomRewardGroupConfig;
import cn.game.protocol.generated.manager.RandomRewardGroupManager;
import cn.game.protocol.generated.manager.RandomRewardManager;
import cn.game.util.Rnd;

public class RandomUtil {
	/**
	 * 根据组ID随机出 奖励组id 再根据奖励组id 获取所有掉落物品和数量
	 * @return
	 */
	public static List<RandomRewardConfig> randomGroupIdItemsByGroupId(int groupId){
		List<RandomRewardConfig> items = new ArrayList<RandomRewardConfig>();
		List<RandomRewardGroupConfig> configs = RandomRewardGroupManager.getInstance().getGroupIdList(groupId);
		if (configs == null) {
			return items;
		}
		
		RandomRewardConfig groupConfig = null;
		
		for (RandomRewardGroupConfig randomRewardGroupConfig : configs) {
			if(Rnd.hitPercentage(randomRewardGroupConfig.getRate())) {
				groupConfig = randomConfigByGroupId(randomRewardGroupConfig.getRandomRewardGroupId());
				items.add(groupConfig);				
			}
		}
		
		return items;
	}
		
	/**
	 * 根据组ID随机出奖励物品
	 * @return
	 */
	public static RandomRewardConfig randomConfigByGroupId(int groupId){
		List<RandomRewardConfig> gCfs = RandomRewardManager.getInstance().getGroupIdList(groupId);
		
		int maxRondomNum = 0;
		int[] randomNums = new int[gCfs.size()];
		for (int i = 0; i < gCfs.size(); i++) {					
			randomNums[i] = gCfs.get(i).getProportion();
			maxRondomNum += randomNums[i];
		}
		
		return gCfs.get(Rnd.randomIndex(maxRondomNum, randomNums));		
	}
			
}
