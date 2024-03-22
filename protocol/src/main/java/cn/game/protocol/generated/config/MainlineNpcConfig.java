package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 主线Npc表
 * 
 * 工具生成的，不要手动修改
 */
 public class MainlineNpcConfig {

	/** id -- id */
	private final int id;		
	/** 地图id -- 地图id */
	private final int mapId;		
	/** 名称 -- 名称 */
	private final String name;		
	/** 描述 -- 描述 */
	private final String desc;		
	/** 资源 -- 资源 */
	private final String resource;		
	/** 默认动作资源 -- 资源 */
	private final String animation;		
	/** 行为模式 -- 1-静止 2-来回走动,停顿3s 3-随机移动,停顿3s */
	private final int behaviorType;		
	/** 移动范围 -- min|max */
	private final List<Integer> movingRange;		
	/** 移动速度 */
	private final int speed;		
	/** 问候语 */
	private final List<String> greetings;		
	/** 对话类型 -- 1.一次性：每次对话时按顺序播放一句文本，全部播放完毕后不可再对话 2.循环：每次对话时按顺序播放一句文本，全部播放完毕后从头开始 3.末尾循环：每次对话时按顺序播放一句文本，全部播放完毕后，后续只播放最后一句 4.随机：每次随机播放一句对话； 5.全部：每次按顺序播放全部对话 */
	private final int talkType;		
	/** 对话文件 */
	private final List<String> dialogueTxt;		
	/** 战场 -- 战场 */
	private final int battlelevel;		
	/** 挑战对话文本 -- 第一条:未曾击败过时的文本 第二条:曾击败过时的文本 */
	private final List<String> challengeTxt;		
	/** 挑战结果文本 -- 第一条:玩家胜利文本 第二条:玩家失败文本 */
	private final List<String> challengeResultTxt;		
	/** TCG卡组 */
	private final int tcgGroup;		
	/** TCG文本 -- 第一条:玩家胜利文本 第二条:玩家失败文本 */
	private final List<String> tcgTxt;		
	/** 商店 -- 关联Store的二级页签 */
	private final int shop;		
	/** 商店类型 -- 1-商店 2-食材店 3-酒馆 */
	private final int shopType;		
	/** 商店文本 -- 第一条:购买后文本 第二条:未购买文本 */
	private final List<String> shopTxt;		
	/** 是否未旅店 -- 1-是 0-否 */
	private final boolean hostel;		
	/** 旅店文本 -- 使用旅店后文本,配置一条即可 */
	private final String hostelTxt;		
	/** 旅店消耗 */
	private final List<Entry<Integer,Integer>> hostelCost;		

	public MainlineNpcConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.mapId = Integer.parseInt(element.getAttribute("mapId") == null || element.getAttribute("mapId").length() == 0 ? "0"
			: element.getAttribute("mapId")); // 地图id
		this.name = element.getAttribute("name"); // 名称
		this.desc = element.getAttribute("desc"); // 描述
		this.resource = element.getAttribute("resource"); // 资源
		this.animation = element.getAttribute("animation"); // 默认动作资源
		this.behaviorType = Integer.parseInt(element.getAttribute("behaviorType") == null || element.getAttribute("behaviorType").length() == 0 ? "0"
			: element.getAttribute("behaviorType")); // 行为模式
		String movingRangeString = element.getAttribute("movingRange"); // 移动范围
		if (movingRangeString != null && movingRangeString.length() > 0) {
			String[] movingRangeStrings = movingRangeString.split("\\|"); 
			List<Integer> movingRange = new ArrayList<Integer>(movingRangeStrings.length) ; 
			for (int i = 0; i < movingRangeStrings.length; i++) {
				Integer temp = Integer.parseInt(movingRangeStrings[i]);
				movingRange.add(temp);
			}
			this.movingRange = com.google.common.collect.ImmutableList.copyOf(movingRange);						
		} else {
			this.movingRange = java.util.Collections.emptyList();
		}
		this.speed = Integer.parseInt(element.getAttribute("speed") == null || element.getAttribute("speed").length() == 0 ? "0"
			: element.getAttribute("speed")); // 移动速度
		String greetingsString = element.getAttribute("greetings"); // 问候语
		if (greetingsString != null && greetingsString.length() > 0) {
			String[] greetingsStrings = greetingsString.split("\\|"); 
			List<String> greetings = new ArrayList<String>(greetingsStrings.length) ; 
			for (int i = 0; i < greetingsStrings.length; i++) {
				String temp = greetingsStrings[i];
				greetings.add(temp);
			}
			this.greetings = com.google.common.collect.ImmutableList.copyOf(greetings);						
		} else {
			this.greetings = java.util.Collections.emptyList();
		}
		this.talkType = Integer.parseInt(element.getAttribute("talkType") == null || element.getAttribute("talkType").length() == 0 ? "0"
			: element.getAttribute("talkType")); // 对话类型
		String dialogueTxtString = element.getAttribute("dialogueTxt"); // 对话文件
		if (dialogueTxtString != null && dialogueTxtString.length() > 0) {
			String[] dialogueTxtStrings = dialogueTxtString.split("\\|"); 
			List<String> dialogueTxt = new ArrayList<String>(dialogueTxtStrings.length) ; 
			for (int i = 0; i < dialogueTxtStrings.length; i++) {
				String temp = dialogueTxtStrings[i];
				dialogueTxt.add(temp);
			}
			this.dialogueTxt = com.google.common.collect.ImmutableList.copyOf(dialogueTxt);						
		} else {
			this.dialogueTxt = java.util.Collections.emptyList();
		}
		this.battlelevel = Integer.parseInt(element.getAttribute("battlelevel") == null || element.getAttribute("battlelevel").length() == 0 ? "0"
			: element.getAttribute("battlelevel")); // 战场
		String challengeTxtString = element.getAttribute("challengeTxt"); // 挑战对话文本
		if (challengeTxtString != null && challengeTxtString.length() > 0) {
			String[] challengeTxtStrings = challengeTxtString.split("\\|"); 
			List<String> challengeTxt = new ArrayList<String>(challengeTxtStrings.length) ; 
			for (int i = 0; i < challengeTxtStrings.length; i++) {
				String temp = challengeTxtStrings[i];
				challengeTxt.add(temp);
			}
			this.challengeTxt = com.google.common.collect.ImmutableList.copyOf(challengeTxt);						
		} else {
			this.challengeTxt = java.util.Collections.emptyList();
		}
		String challengeResultTxtString = element.getAttribute("challengeResultTxt"); // 挑战结果文本
		if (challengeResultTxtString != null && challengeResultTxtString.length() > 0) {
			String[] challengeResultTxtStrings = challengeResultTxtString.split("\\|"); 
			List<String> challengeResultTxt = new ArrayList<String>(challengeResultTxtStrings.length) ; 
			for (int i = 0; i < challengeResultTxtStrings.length; i++) {
				String temp = challengeResultTxtStrings[i];
				challengeResultTxt.add(temp);
			}
			this.challengeResultTxt = com.google.common.collect.ImmutableList.copyOf(challengeResultTxt);						
		} else {
			this.challengeResultTxt = java.util.Collections.emptyList();
		}
		this.tcgGroup = Integer.parseInt(element.getAttribute("tcgGroup") == null || element.getAttribute("tcgGroup").length() == 0 ? "0"
			: element.getAttribute("tcgGroup")); // TCG卡组
		String tcgTxtString = element.getAttribute("tcgTxt"); // TCG文本
		if (tcgTxtString != null && tcgTxtString.length() > 0) {
			String[] tcgTxtStrings = tcgTxtString.split("\\|"); 
			List<String> tcgTxt = new ArrayList<String>(tcgTxtStrings.length) ; 
			for (int i = 0; i < tcgTxtStrings.length; i++) {
				String temp = tcgTxtStrings[i];
				tcgTxt.add(temp);
			}
			this.tcgTxt = com.google.common.collect.ImmutableList.copyOf(tcgTxt);						
		} else {
			this.tcgTxt = java.util.Collections.emptyList();
		}
		this.shop = Integer.parseInt(element.getAttribute("shop") == null || element.getAttribute("shop").length() == 0 ? "0"
			: element.getAttribute("shop")); // 商店
		this.shopType = Integer.parseInt(element.getAttribute("shopType") == null || element.getAttribute("shopType").length() == 0 ? "0"
			: element.getAttribute("shopType")); // 商店类型
		String shopTxtString = element.getAttribute("shopTxt"); // 商店文本
		if (shopTxtString != null && shopTxtString.length() > 0) {
			String[] shopTxtStrings = shopTxtString.split("\\|"); 
			List<String> shopTxt = new ArrayList<String>(shopTxtStrings.length) ; 
			for (int i = 0; i < shopTxtStrings.length; i++) {
				String temp = shopTxtStrings[i];
				shopTxt.add(temp);
			}
			this.shopTxt = com.google.common.collect.ImmutableList.copyOf(shopTxt);						
		} else {
			this.shopTxt = java.util.Collections.emptyList();
		}
		this.hostel = Boolean.parseBoolean(element.getAttribute("hostel") == null || element.getAttribute("hostel").length() == 0 ? "false"
			: element.getAttribute("hostel")); // 是否未旅店
		this.hostelTxt = element.getAttribute("hostelTxt"); // 旅店文本
		String hostelCostString = element.getAttribute("hostelCost"); // 旅店消耗
		if (hostelCostString != null && hostelCostString.length() > 0) {
			String[] hostelCostStrings = hostelCostString.split("\\|"); 
			List<Entry<Integer,Integer>> hostelCost = new ArrayList<Entry<Integer,Integer>>(hostelCostStrings.length) ; 
			for (int i = 0; i < hostelCostStrings.length; i++) {
			    String[] split = hostelCostStrings[i].split(":", 2);
				hostelCost.add(new Entry<Integer,Integer>()	{
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

			this.hostelCost = com.google.common.collect.ImmutableList.copyOf(hostelCost);						
		} else {
			this.hostelCost = java.util.Collections.emptyList();
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
	
	public String getDesc() {
		return desc;
	}
	
	public String getResource() {
		return resource;
	}
	
	public String getAnimation() {
		return animation;
	}
	
	public int getBehaviorType() {
		return behaviorType;
	}
	
	public List<Integer> getMovingRange() {
		return movingRange;
	}
	
	public int getSpeed() {
		return speed;
	}
	
	public List<String> getGreetings() {
		return greetings;
	}
	
	public int getTalkType() {
		return talkType;
	}
	
	public List<String> getDialogueTxt() {
		return dialogueTxt;
	}
	
	public int getBattlelevel() {
		return battlelevel;
	}
	
	public List<String> getChallengeTxt() {
		return challengeTxt;
	}
	
	public List<String> getChallengeResultTxt() {
		return challengeResultTxt;
	}
	
	public int getTcgGroup() {
		return tcgGroup;
	}
	
	public List<String> getTcgTxt() {
		return tcgTxt;
	}
	
	public int getShop() {
		return shop;
	}
	
	public int getShopType() {
		return shopType;
	}
	
	public List<String> getShopTxt() {
		return shopTxt;
	}
	
	public boolean getHostel() {
		return hostel;
	}
	
	public String getHostelTxt() {
		return hostelTxt;
	}
	
	public List<Entry<Integer,Integer>> getHostelCost() {
		return hostelCost;
	}
	
}
