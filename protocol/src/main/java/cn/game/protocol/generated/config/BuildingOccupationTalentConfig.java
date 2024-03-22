package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 职业天赋(分析室)
 * 
 * 工具生成的，不要手动修改
 */
 public class BuildingOccupationTalentConfig {

	/** id */
	private int id;		
	/** 职业类型 */
	private int occupation;		
	/** 节点类型 */
	private int nodeType;		
	/** 所属节点 */
	private int nodeInherit;		
	/** 前置条件 */
	private int frontCondition;		
	/** SillBuff表 */
	private int buff;		
	/** 节点消耗 */
	private List<Entry<Integer,Integer>> nodeCost;		

	public BuildingOccupationTalentConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.occupation = Integer.parseInt(element.getAttribute("occupation") == null || element.getAttribute("occupation").length() == 0 ? "0"
			: element.getAttribute("occupation")); // 职业类型
		this.nodeType = Integer.parseInt(element.getAttribute("nodeType") == null || element.getAttribute("nodeType").length() == 0 ? "0"
			: element.getAttribute("nodeType")); // 节点类型
		this.nodeInherit = Integer.parseInt(element.getAttribute("nodeInherit") == null || element.getAttribute("nodeInherit").length() == 0 ? "0"
			: element.getAttribute("nodeInherit")); // 所属节点
		this.frontCondition = Integer.parseInt(element.getAttribute("frontCondition") == null || element.getAttribute("frontCondition").length() == 0 ? "0"
			: element.getAttribute("frontCondition")); // 前置条件
		this.buff = Integer.parseInt(element.getAttribute("buff") == null || element.getAttribute("buff").length() == 0 ? "0"
			: element.getAttribute("buff")); // SillBuff表
		String nodeCostString = element.getAttribute("nodeCost"); // 节点消耗
		if (nodeCostString != null && nodeCostString.length() > 0) {
			String[] nodeCostStrings = nodeCostString.split("\\|"); 
			this.nodeCost = new ArrayList<Entry<Integer,Integer>>(nodeCostStrings.length) ; 
			for (int i = 0; i < nodeCostStrings.length; i++) {
			    String[] split = nodeCostStrings[i].split(":", 2);
				this.nodeCost.add(new Entry<Integer,Integer>()	{
					@Override
					public Integer setValue(Integer value) {
						return null;
					}
					@Override
					public Integer getValue() {
						try {
							return Integer.parseInt(split[1]);
						} catch (Exception e) {
							e.printStackTrace();
						}
						throw new NullPointerException();	
					}
					@Override
					public Integer getKey() {
						try {
							return Integer.parseInt(split[0]);
						} catch (Exception e) {
							e.printStackTrace();
						}
						throw new NullPointerException();
					}
				}) ; 
			}
		} else {
			this.nodeCost = new ArrayList<Entry<Integer,Integer>>();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public int getOccupation() {
		return occupation;
	}
	
	public int getNodeType() {
		return nodeType;
	}
	
	public int getNodeInherit() {
		return nodeInherit;
	}
	
	public int getFrontCondition() {
		return frontCondition;
	}
	
	public int getBuff() {
		return buff;
	}
	
	public List<Entry<Integer,Integer>> getNodeCost() {
		return nodeCost;
	}
	
}
