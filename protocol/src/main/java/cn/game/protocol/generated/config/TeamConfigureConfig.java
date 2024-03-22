package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 队伍配置
 * 
 * 工具生成的，不要手动修改
 */
 public class TeamConfigureConfig {

	/** id */
	private final int id;		
	/** 玩法类型 */
	private final List<Integer> dungeonType;		

	public TeamConfigureConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		String dungeonTypeString = element.getAttribute("dungeonType"); // 玩法类型
		if (dungeonTypeString != null && dungeonTypeString.length() > 0) {
			String[] dungeonTypeStrings = dungeonTypeString.split("\\|"); 
			List<Integer> dungeonType = new ArrayList<Integer>(dungeonTypeStrings.length) ; 
			for (int i = 0; i < dungeonTypeStrings.length; i++) {
				Integer temp = Integer.parseInt(dungeonTypeStrings[i]);
				dungeonType.add(temp);
			}
			this.dungeonType = com.google.common.collect.ImmutableList.copyOf(dungeonType);						
		} else {
			this.dungeonType = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public List<Integer> getDungeonType() {
		return dungeonType;
	}
	
}
