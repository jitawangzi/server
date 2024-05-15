package cn.game.games.net.game.helper;

import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.module.award.RewardItem;
import cn.game.protocol.generated.config.DropConfig;
import cn.game.protocol.generated.manager.DropManager;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.Pair;
import cn.game.util.Rnd;

/**   
 * @Description 掉落帮助类
 * @date 2018年11月29日 下午3:58:46
 * @author SYQ
 */
public class DropHelper {

	/**
	 * @Description  根据掉落id获取具体的奖励
	 * @param dropId  DropConfig
	 * @return
	 */
	public static List<RewardItem> drop(int dropId) {

		DropConfig dropConfig = DropManager.getInstance().getDropConfig(dropId);

		if (dropConfig == null) {
			throw new IllegalArgumentException("掉落id不存在: " + dropId);
		}
		List<RewardItem> ret = new ArrayList<>();
		if (dropConfig.getDropType() == 1) {
			int maxCount = dropConfig.getMaxCount();
			for (int i = 0, size = dropConfig.getWeight().size(); i < size; i++) {
				if (maxCount > 0 && ret.size() >= maxCount) {
					return ret;
				}
				if (Rnd.hit(dropConfig.getWeight().get(i))) {
					RewardItem item = new RewardItem();
					item.setId(dropConfig.getItemId().get(i));
					item.setCount(dropConfig.getCount().get(i));
					ret.add(item);
				}

			}
		} else if (dropConfig.getDropType() == 2) {
			int randomIndex = Rnd.randomIndex(dropConfig.getWeight());
			RewardItem item = new RewardItem();
			item.setId(dropConfig.getItemId().get(randomIndex));
			item.setCount(dropConfig.getCount().get(randomIndex));
			ret.add(item);
		} else {
			throw new IllegalArgumentException("掉落类型错误 : " + dropId);
		}
		return ret;
	}

	public static List<RewardItem> drop(List<Integer> ids) {

		List<RewardItem> ret = new ArrayList<>();

		for (Integer e : ids) {
			ret.addAll(drop(e));
		}
		return ret;
	}

	public static Collection<RewardInfo> drop(Player player, int dropId) {

		List<RewardItem> drop = DropHelper.drop(dropId);
		Set<Pair<Integer, Integer>> rewards = drop.stream()
				.map(item -> new Pair<Integer, Integer>(item.getId(), item.getCount())).collect(toSet());
		return PlayerHelper.addResources(player, rewards, null);
	}

	public static List<RewardInfo> drop(Player player, List<Integer> dropIds) {
		List<RewardItem> drop = DropHelper.drop(dropIds);
		Set<Pair<Integer, Integer>> rewards = drop.stream()
				.map(item -> new Pair<Integer, Integer>(item.getId(), item.getCount())).collect(toSet());
		return PlayerHelper.addResources(player, rewards, null);
	}

}
