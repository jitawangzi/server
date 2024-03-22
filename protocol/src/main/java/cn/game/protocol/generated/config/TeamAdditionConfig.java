package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 探索意志
 * 
 * 工具生成的，不要手动修改
 */
 public class TeamAdditionConfig {

	/** id -- id */
	private final int id;		
	/** 地貌 -- 1-工业区 2-流放区 3-乐园区 4-地下区 */
	private final int landforms;		
	/** 类别 -- 1-组1 2-组2 3-组3 4-组4 5-组5 6-组6 一组的节点不要超过6个 */
	private final int group;		
	/** 前置条件 -- 代表当前节点得前一个节点 */
	private final int preNode;		
	/** buff效果 -- 读取buff表ID */
	private final List<Integer> buffIds;		

	public TeamAdditionConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.landforms = Integer.parseInt(element.getAttribute("landforms") == null || element.getAttribute("landforms").length() == 0 ? "0"
			: element.getAttribute("landforms")); // 地貌
		this.group = Integer.parseInt(element.getAttribute("group") == null || element.getAttribute("group").length() == 0 ? "0"
			: element.getAttribute("group")); // 类别
		this.preNode = Integer.parseInt(element.getAttribute("preNode") == null || element.getAttribute("preNode").length() == 0 ? "0"
			: element.getAttribute("preNode")); // 前置条件
		String buffIdsString = element.getAttribute("buffIds"); // buff效果
		if (buffIdsString != null && buffIdsString.length() > 0) {
			String[] buffIdsStrings = buffIdsString.split("\\|"); 
			List<Integer> buffIds = new ArrayList<Integer>(buffIdsStrings.length) ; 
			for (int i = 0; i < buffIdsStrings.length; i++) {
				Integer temp = Integer.parseInt(buffIdsStrings[i]);
				buffIds.add(temp);
			}
			this.buffIds = com.google.common.collect.ImmutableList.copyOf(buffIds);						
		} else {
			this.buffIds = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public int getLandforms() {
		return landforms;
	}
	
	public int getGroup() {
		return group;
	}
	
	public int getPreNode() {
		return preNode;
	}
	
	public List<Integer> getBuffIds() {
		return buffIds;
	}
	
}
