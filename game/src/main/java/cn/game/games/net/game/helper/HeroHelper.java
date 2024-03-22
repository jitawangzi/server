package cn.game.games.net.game.helper;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.protocol.generated.config.EquipAttributeConfig;
import cn.game.protocol.generated.config.EquipConfig;
import cn.game.protocol.generated.config.PlayerLevelConfig;
import cn.game.protocol.generated.manager.EquipAttributeManager;
import cn.game.protocol.generated.manager.PlayerLevelManager;
import cn.game.util.Rnd;

public class HeroHelper {

	private static final Logger log = LoggerFactory.getLogger(HeroHelper.class);

	public static int randomEquipAttr(List<Entry<Integer, Integer>> list, List<Integer> excludeTypes) {

		int total = 0;
		for (Entry<Integer, Integer> entry : list) {
			total += entry.getValue();
		}
		int rand = Rnd.nextInt(total);
		int current = 0;

		for (Entry<Integer, Integer> entry : list) {
			current += entry.getValue();
			EquipAttributeConfig equipAttributeConfig = EquipAttributeManager.getInstance().getEquipAttributeConfig(entry.getKey());
			if (excludeTypes.contains(equipAttributeConfig.getAtr())) {
				continue;
			}
			if (rand < current) {
				return entry.getKey();
			}
		}
		return 0;
	}

	/*public static List<EquipAttrInfo> buildEquipAttrInfo(List<EquipAttr> list) {

		List<EquipAttrInfo> ret = new ArrayList<>(list.size());

		for (EquipAttr e : list) {
			EquipAttrInfo.Builder builder = EquipAttrInfo.newBuilder();

			EquipAttributeConfig equipAttributeConfig = EquipAttributeManager.getInstance().getEquipAttributeConfig(e.getId());
			builder.setId(equipAttributeConfig.getId());
			builder.setInit(e.getInit());
			builder.setLevel(e.getLevel());

			// int value = equipAttributeConfig.getAtrGrow().get(e.getLevel()) *
			// e.getLevel() + e.getInit();
			// builder.setValue(value);
			ret.add(builder.build());
		}
		return ret;
	}*/

	public static int attrInit(List<Integer> list) {

		if (list.size() == 1) {
			return list.get(0);
		}
		int min = list.get(0);
		int max = list.get(1);
		int r = list.get(2);
		if (r == 0) {
			return min;
		}
		int count = (max - min) / r + 1;
		int rand = Rnd.nextInt(count);
		int current = 0;

		for (int i = min; i <= max; i += r) {
			if (rand <= current) {
				return i;
			}
			current += 1;
		}
		return 0;
	}

	public static boolean checkProf(int level, int prof) {
		Collection<PlayerLevelConfig> list = PlayerLevelManager.getInstance().list();

		for (PlayerLevelConfig e : list) {
			if (e.getUnlockProf() == prof) {
				if (level >= e.getId()) {
					return true;
				}
			}
		}
		return false;
	}


	public static boolean isWeapon(EquipConfig equipConfig) {
		return equipConfig.getSubType() == 9;
	}

	/**
	 * @Description  设置主角星级
	 * @param playerId
	 * @return
	 */
	public static int setMainHeroStar(long playerId) {
//		Player player = PlayerManager.getInstance().getPlayer(playerId);
//		Byte taluopai = player.getData().getTaluopai();
//		List<Entry<Integer, Integer>> atr_growth = new ArrayList<>();
//		if (taluopai != null) {
//			ChiefInitConfig chiefInitConfig = ChiefInitManager.getInstance().getChiefInitConfig(taluopai);
//			if (chiefInitConfig != null) {
//				atr_growth.addAll(chiefInitConfig.getAtr_growth());
//			}
//		}
//		StoryOp storyOp = player.getModule(StoryOp.class);
//		for (List<Story> list : storyOp.list().values()) {
//			for (Story e : list) {
//				StoryOptionConfig storyOptionConfig = StoryOptionManager.getInstance().getStoryOptionConfig(e.getOpt());
//				if (storyOptionConfig != null) {
//					if (storyOptionConfig.getChiefGrow() > 0) {
//						ChiefInitConfig chiefInitConfig = ChiefInitManager.getInstance()
//								.getChiefInitConfig(storyOptionConfig.getChiefGrow());
//						if (chiefInitConfig != null) {
//							atr_growth.addAll(chiefInitConfig.getAtr_growth());
//						}
//					}
//				}
//			}
//		}
//
//		int value = 0;
//		for (Entry<Integer, Integer> e : atr_growth) {
//			value += e.getValue();
//		}
		int star = 1;
//		String value2 = GlobalConf.account_hero_chief_star.getValue();
//		String[] split = value2.split(";");
//
//		for (int i = 0; i < split.length; i++) {
//			if (value >= Integer.parseInt(split[i])) {
//				star++;
//			}
//		}
//		// System.err.println(value);
//		HeroOp heroOp = player.getModule(HeroOp.class);
//		Hero hero = heroOp.get(player.getData().getHeroId());
//		if (hero != null) {
//			hero.setStar(star);
//		}
		return star;
	}

}
