import java.util.HashMap;
import java.util.Map;

import cn.game.games.cache.entity.Quest;
import cn.game.protocol.generated.enume.QuestTypeEnum;
import cn.game.util.JsonUtil;

public class GG {
	public static void main(String[] args) {

		Map<Integer, Quest>[] quests = null;

		quests = new HashMap[QuestTypeEnum.values().length];
		for (int i = 0; i < quests.length; i++) {
			quests[i] = new HashMap<>();
		}
		quests[0].put(22, new Quest(3333, 23));
		
		String jsonString = JsonUtil.toJsonString(quests); 
		System.out.println(jsonString);
		
		Map<Integer, Quest>[] map = (Map<Integer, Quest>[]) JsonUtil.parseObject(jsonString, Object.class);
		System.out.println(map[0].get("22")); // 获取到了正确的值
		System.out.println(map[0].get(22)); // 获取到的值为null
		

	}
}
