package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 后勤部表
 * 
 * 工具生成的，不要手动修改
 */
 public class TechnologyTreeNodeConfig {

	/** id -- id */
	private final int id;		
	/** 备注 */
	private final String des;		
	/** 图标 */
	private final String icon;		
	/** 所属组（流派/条） -- 1-从上往下第一条 2-从上往下第二条 3-从上往下第三条 4-从上往下第四条 5-从上往下第五条 */
	private final int group;		
	/** 前置条件 */
	private final int preNode;		
	/** 节点消耗 */
	private final List<Entry<Integer,Integer>> cost;		
	/** 效果类型 -- 科技线枚举表 */
	private final int effectType;		
	/** 效果参数 */
	private final int[] param;		

	public TechnologyTreeNodeConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.des = element.getAttribute("des"); // 备注
		this.icon = element.getAttribute("icon"); // 图标
		this.group = Integer.parseInt(element.getAttribute("group") == null || element.getAttribute("group").length() == 0 ? "0"
			: element.getAttribute("group")); // 所属组（流派/条）
		this.preNode = Integer.parseInt(element.getAttribute("preNode") == null || element.getAttribute("preNode").length() == 0 ? "0"
			: element.getAttribute("preNode")); // 前置条件
		String costString = element.getAttribute("cost"); // 节点消耗
		if (costString != null && costString.length() > 0) {
			String[] costStrings = costString.split("\\|"); 
			List<Entry<Integer,Integer>> cost = new ArrayList<Entry<Integer,Integer>>(costStrings.length) ; 
			for (int i = 0; i < costStrings.length; i++) {
			    String[] split = costStrings[i].split(":", 2);
				cost.add(new Entry<Integer,Integer>()	{
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

			this.cost = com.google.common.collect.ImmutableList.copyOf(cost);						
		} else {
			this.cost = java.util.Collections.emptyList();
		}
		this.effectType = Integer.parseInt(element.getAttribute("effectType") == null || element.getAttribute("effectType").length() == 0 ? "0"
			: element.getAttribute("effectType")); // 效果类型
		String paramString = element.getAttribute("param"); // 效果参数
		if (paramString != null && paramString.length() > 0) {
			String[] paramStrings = paramString.split("\\|"); 
			int[] param = new int[paramStrings.length] ; 
			for (int i = 0; i < paramStrings.length; i++) {
				int temp = Integer.parseInt(paramStrings[i]);
				param[i] = temp;
			}
			this.param = param ;			
		} else {
			this.param = new int[] {};
		}
	}
	
	public int getId() {
		return id;
	}
	
	public String getDes() {
		return des;
	}
	
	public String getIcon() {
		return icon;
	}
	
	public int getGroup() {
		return group;
	}
	
	public int getPreNode() {
		return preNode;
	}
	
	public List<Entry<Integer,Integer>> getCost() {
		return cost;
	}
	
	public int getEffectType() {
		return effectType;
	}
	
	public int[] getParam() {
		return param;
	}
	
}
