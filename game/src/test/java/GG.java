import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.game.protocol.generated.config.QuestConfig;

public class GG {
	public static void main(String[] args) {

		Map<Integer, List<QuestConfig>> Types = new HashMap<>();
		Types = com.google.common.collect.ImmutableMap.copyOf(Types);
		Types.remove(0);

	}
}
