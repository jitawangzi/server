package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 源质节点解锁顺序
 * 
 * 工具生成的，不要手动修改
 */
 public class OriginUnlockConfig {

	/** id */
	private final int id;		
	/** 源质点解锁条件 */
	private final List<Integer> originIds;		
	/** 解锁等级 */
	private final int level;		

	public OriginUnlockConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		String originIdsString = element.getAttribute("originIds"); // 源质点解锁条件
		if (originIdsString != null && originIdsString.length() > 0) {
			String[] originIdsStrings = originIdsString.split("\\|"); 
			List<Integer> originIds = new ArrayList<Integer>(originIdsStrings.length) ; 
			for (int i = 0; i < originIdsStrings.length; i++) {
				Integer temp = Integer.parseInt(originIdsStrings[i]);
				originIds.add(temp);
			}
			this.originIds = com.google.common.collect.ImmutableList.copyOf(originIds);						
		} else {
			this.originIds = java.util.Collections.emptyList();
		}
		this.level = Integer.parseInt(element.getAttribute("level") == null || element.getAttribute("level").length() == 0 ? "0"
			: element.getAttribute("level")); // 解锁等级
	}
	
	public int getId() {
		return id;
	}
	
	public List<Integer> getOriginIds() {
		return originIds;
	}
	
	public int getLevel() {
		return level;
	}
	
}
