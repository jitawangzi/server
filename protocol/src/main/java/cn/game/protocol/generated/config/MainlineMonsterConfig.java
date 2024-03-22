package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 主线怪物表
 * 
 * 工具生成的，不要手动修改
 */
 public class MainlineMonsterConfig {

	/** id -- id */
	private final int id;		
	/** 地图id -- 地图id */
	private final int mapId;		
	/** 怪物名称 -- 怪物名称 */
	private final String name;		
	/** 资源 -- 资源 */
	private final String resource;		
	/** 战场 -- 战场 */
	private final int battlelevel;		
	/** 类型 -- 类型 */
	private final int type;		
	/** 怪物奖励 -- Boss怪物奖励 */
	private final List<Entry<Integer,Integer>> monsterReward;		

	public MainlineMonsterConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.mapId = Integer.parseInt(element.getAttribute("mapId") == null || element.getAttribute("mapId").length() == 0 ? "0"
			: element.getAttribute("mapId")); // 地图id
		this.name = element.getAttribute("name"); // 怪物名称
		this.resource = element.getAttribute("resource"); // 资源
		this.battlelevel = Integer.parseInt(element.getAttribute("battlelevel") == null || element.getAttribute("battlelevel").length() == 0 ? "0"
			: element.getAttribute("battlelevel")); // 战场
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 类型
		String monsterRewardString = element.getAttribute("monsterReward"); // 怪物奖励
		if (monsterRewardString != null && monsterRewardString.length() > 0) {
			String[] monsterRewardStrings = monsterRewardString.split("\\|"); 
			List<Entry<Integer,Integer>> monsterReward = new ArrayList<Entry<Integer,Integer>>(monsterRewardStrings.length) ; 
			for (int i = 0; i < monsterRewardStrings.length; i++) {
			    String[] split = monsterRewardStrings[i].split(":", 2);
				monsterReward.add(new Entry<Integer,Integer>()	{
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

			this.monsterReward = com.google.common.collect.ImmutableList.copyOf(monsterReward);						
		} else {
			this.monsterReward = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public int getMapId() {
		return mapId;
	}
	
	public String getName() {
		return name;
	}
	
	public String getResource() {
		return resource;
	}
	
	public int getBattlelevel() {
		return battlelevel;
	}
	
	public int getType() {
		return type;
	}
	
	public List<Entry<Integer,Integer>> getMonsterReward() {
		return monsterReward;
	}
	
}
