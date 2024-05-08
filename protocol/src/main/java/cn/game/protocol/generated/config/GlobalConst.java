package cn.game.protocol.generated.config;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 常量表
 * 
 * 工具生成的，不要手动修改
 */
public class GlobalConst extends ResourceListener {

	private static final String xmlFileName = "GlobalConst";
	private static GlobalConst instance = new GlobalConst();

	/** 【初始化物品】 */
	public static int[][] initItems;		
	/** 【UID创建取值】参数1版本标识，参数2自增函数起始值 */
	public static int[] CreateUID;		
	/** 【肉鸽】全场AOE类肉鸽规定 */
	public static int[] Rogueroll1;		
	/** 【肉鸽】全场纯加属性类肉鸽规定 */
	public static int[] Rogueroll2;		
	/** 【彩蛋肉鸽】触发次数与几率 */
	public static int[] RogueEasterEgg;		
	/** 【彩蛋肉鸽】肉鸽组 */
	public static int[] RogueEasterEggGroup;		
	/** 【英雄升级】所有职业消耗相同物品 */
	public static int HeroLvItem;		
	/** 【图鉴】拥有&每次突破，每个神将固定奖励元宝数 */
	public static int HeroBookAward;		
	/** 【图鉴】不同品质的1颗星加成属性id */
	public static Map<Integer,Integer> HeroBookStar;		
	/** 【随机小云宝箱】触发 */
	public static int[] RandomCLoud;		
	/** 【随机小云宝箱】显示 */
	public static int[][] RandomCLoudAward;		
	/** 【队长加成】 */
	public static int[][] CaptainBonus;		
	/** 每日任务宝箱积分 */
	public static int[] DailyPoint;		
	/** 每日任务宝箱奖励 */
	public static int[][] DailyTask;		
	/** 每周任务宝箱积分 */
	public static int[] WeeklyPoint;		
	/** 每周任务宝箱奖励 */
	public static int[][] WeeklyTask;		
	/** 快速巡逻时长 */
	public static int QuickPatrolDuration;		
	/** 巡逻时长上限 */
	public static int MaximumPatrolDuration;		
	/** 快速巡逻次数 */
	public static int QuickPatrolCnt;		
	/** 月卡附加次数 */
	public static int MonthCardCnt;		
	/** 快速巡逻消耗 */
	public static int QuickPatrolConsume;		
	/** 广告巡逻次数 */
	public static int AdPatrolCnt;		
	/** 日租卡选取数量 */
	public static int DayCardCnt;		

	static {
		WatchServiceManager.getInstance().register(instance);
	}
	
	public static GlobalConst instance() {
		return instance ; 
	}
	
	public static void init (Element element) throws Exception {	
		String initItemsString = element.getAttribute("initItems"); // 【初始化物品】
		if (initItemsString != null && initItemsString.length() > 0) {
			String[] initItemsStrings = initItemsString.split("\\|"); 
			int[][] initItemsTemp = new int[initItemsStrings.length][] ; 
			for (int i = 0; i < initItemsStrings.length; i++) {
				String[] initItemsStrings2 = initItemsStrings[i].split(";"); 
				int[] array = new int[initItemsStrings2.length];
				for (int j = 0; j < initItemsStrings2.length; j++) {
					int temp = Integer.parseInt(initItemsStrings2[j]);	
					array[j] = temp;
				}
				initItemsTemp[i] = array;
			}
			initItems = initItemsTemp ;			
		} else {
			initItems = new int[][] {};
		}
		String CreateUIDString = element.getAttribute("CreateUID"); // 【UID创建取值】参数1版本标识，参数2自增函数起始值
		if (CreateUIDString != null && CreateUIDString.length() > 0) {
			String[] CreateUIDStrings = CreateUIDString.split(";"); 
			int[] CreateUIDTemp = new int[CreateUIDStrings.length] ; 
			for (int i = 0; i < CreateUIDStrings.length; i++) {
				int temp = Integer.parseInt(CreateUIDStrings[i]);	
				CreateUIDTemp[i] = temp;
			}
			CreateUID = CreateUIDTemp ;			
		} else {
			CreateUID = new int[] {};
		}
		String Rogueroll1String = element.getAttribute("Rogueroll1"); // 【肉鸽】全场AOE类肉鸽规定
		if (Rogueroll1String != null && Rogueroll1String.length() > 0) {
			String[] Rogueroll1Strings = Rogueroll1String.split(";"); 
			int[] Rogueroll1Temp = new int[Rogueroll1Strings.length] ; 
			for (int i = 0; i < Rogueroll1Strings.length; i++) {
				int temp = Integer.parseInt(Rogueroll1Strings[i]);	
				Rogueroll1Temp[i] = temp;
			}
			Rogueroll1 = Rogueroll1Temp ;			
		} else {
			Rogueroll1 = new int[] {};
		}
		String Rogueroll2String = element.getAttribute("Rogueroll2"); // 【肉鸽】全场纯加属性类肉鸽规定
		if (Rogueroll2String != null && Rogueroll2String.length() > 0) {
			String[] Rogueroll2Strings = Rogueroll2String.split(";"); 
			int[] Rogueroll2Temp = new int[Rogueroll2Strings.length] ; 
			for (int i = 0; i < Rogueroll2Strings.length; i++) {
				int temp = Integer.parseInt(Rogueroll2Strings[i]);	
				Rogueroll2Temp[i] = temp;
			}
			Rogueroll2 = Rogueroll2Temp ;			
		} else {
			Rogueroll2 = new int[] {};
		}
		String RogueEasterEggString = element.getAttribute("RogueEasterEgg"); // 【彩蛋肉鸽】触发次数与几率
		if (RogueEasterEggString != null && RogueEasterEggString.length() > 0) {
			String[] RogueEasterEggStrings = RogueEasterEggString.split(";"); 
			int[] RogueEasterEggTemp = new int[RogueEasterEggStrings.length] ; 
			for (int i = 0; i < RogueEasterEggStrings.length; i++) {
				int temp = Integer.parseInt(RogueEasterEggStrings[i]);	
				RogueEasterEggTemp[i] = temp;
			}
			RogueEasterEgg = RogueEasterEggTemp ;			
		} else {
			RogueEasterEgg = new int[] {};
		}
		String RogueEasterEggGroupString = element.getAttribute("RogueEasterEggGroup"); // 【彩蛋肉鸽】肉鸽组
		if (RogueEasterEggGroupString != null && RogueEasterEggGroupString.length() > 0) {
			String[] RogueEasterEggGroupStrings = RogueEasterEggGroupString.split(";"); 
			int[] RogueEasterEggGroupTemp = new int[RogueEasterEggGroupStrings.length] ; 
			for (int i = 0; i < RogueEasterEggGroupStrings.length; i++) {
				int temp = Integer.parseInt(RogueEasterEggGroupStrings[i]);	
				RogueEasterEggGroupTemp[i] = temp;
			}
			RogueEasterEggGroup = RogueEasterEggGroupTemp ;			
		} else {
			RogueEasterEggGroup = new int[] {};
		}
		HeroLvItem = Integer.parseInt(element.getAttribute("HeroLvItem") == null || element.getAttribute("HeroLvItem").length() == 0 ? "0"
			: element.getAttribute("HeroLvItem")); // 【英雄升级】所有职业消耗相同物品
		HeroBookAward = Integer.parseInt(element.getAttribute("HeroBookAward") == null || element.getAttribute("HeroBookAward").length() == 0 ? "0"
			: element.getAttribute("HeroBookAward")); // 【图鉴】拥有&每次突破，每个神将固定奖励元宝数
		String HeroBookStarString = element.getAttribute("HeroBookStar"); // 【图鉴】不同品质的1颗星加成属性id
		if (HeroBookStarString != null && HeroBookStarString.length() > 0) {
			String[] HeroBookStarStrings = HeroBookStarString.split("\\|"); 
			Map<Integer,Integer> HeroBookStarTemp = new HashMap<Integer,Integer>(HeroBookStarStrings.length) ; 
			for (int i = 0; i < HeroBookStarStrings.length; i++) {
				String[] split = HeroBookStarStrings[i].split(";", 2);
				Integer key = Integer.parseInt(split[0]);
				Integer value = Integer.parseInt(split[1]);
				HeroBookStarTemp.put(key, value) ; 				
			}
			HeroBookStar = com.google.common.collect.ImmutableMap.copyOf(HeroBookStarTemp);
		}else{
			HeroBookStar = java.util.Collections.emptyMap() ; 
		}
		String RandomCLoudString = element.getAttribute("RandomCLoud"); // 【随机小云宝箱】触发
		if (RandomCLoudString != null && RandomCLoudString.length() > 0) {
			String[] RandomCLoudStrings = RandomCLoudString.split(";"); 
			int[] RandomCLoudTemp = new int[RandomCLoudStrings.length] ; 
			for (int i = 0; i < RandomCLoudStrings.length; i++) {
				int temp = Integer.parseInt(RandomCLoudStrings[i]);	
				RandomCLoudTemp[i] = temp;
			}
			RandomCLoud = RandomCLoudTemp ;			
		} else {
			RandomCLoud = new int[] {};
		}
		String RandomCLoudAwardString = element.getAttribute("RandomCLoudAward"); // 【随机小云宝箱】显示
		if (RandomCLoudAwardString != null && RandomCLoudAwardString.length() > 0) {
			String[] RandomCLoudAwardStrings = RandomCLoudAwardString.split("\\|"); 
			int[][] RandomCLoudAwardTemp = new int[RandomCLoudAwardStrings.length][] ; 
			for (int i = 0; i < RandomCLoudAwardStrings.length; i++) {
				String[] RandomCLoudAwardStrings2 = RandomCLoudAwardStrings[i].split(";"); 
				int[] array = new int[RandomCLoudAwardStrings2.length];
				for (int j = 0; j < RandomCLoudAwardStrings2.length; j++) {
					int temp = Integer.parseInt(RandomCLoudAwardStrings2[j]);	
					array[j] = temp;
				}
				RandomCLoudAwardTemp[i] = array;
			}
			RandomCLoudAward = RandomCLoudAwardTemp ;			
		} else {
			RandomCLoudAward = new int[][] {};
		}
		String CaptainBonusString = element.getAttribute("CaptainBonus"); // 【队长加成】
		if (CaptainBonusString != null && CaptainBonusString.length() > 0) {
			String[] CaptainBonusStrings = CaptainBonusString.split("\\|"); 
			int[][] CaptainBonusTemp = new int[CaptainBonusStrings.length][] ; 
			for (int i = 0; i < CaptainBonusStrings.length; i++) {
				String[] CaptainBonusStrings2 = CaptainBonusStrings[i].split(";"); 
				int[] array = new int[CaptainBonusStrings2.length];
				for (int j = 0; j < CaptainBonusStrings2.length; j++) {
					int temp = Integer.parseInt(CaptainBonusStrings2[j]);	
					array[j] = temp;
				}
				CaptainBonusTemp[i] = array;
			}
			CaptainBonus = CaptainBonusTemp ;			
		} else {
			CaptainBonus = new int[][] {};
		}
		String DailyPointString = element.getAttribute("DailyPoint"); // 每日任务宝箱积分
		if (DailyPointString != null && DailyPointString.length() > 0) {
			String[] DailyPointStrings = DailyPointString.split(";"); 
			int[] DailyPointTemp = new int[DailyPointStrings.length] ; 
			for (int i = 0; i < DailyPointStrings.length; i++) {
				int temp = Integer.parseInt(DailyPointStrings[i]);	
				DailyPointTemp[i] = temp;
			}
			DailyPoint = DailyPointTemp ;			
		} else {
			DailyPoint = new int[] {};
		}
		String DailyTaskString = element.getAttribute("DailyTask"); // 每日任务宝箱奖励
		if (DailyTaskString != null && DailyTaskString.length() > 0) {
			String[] DailyTaskStrings = DailyTaskString.split("\\|"); 
			int[][] DailyTaskTemp = new int[DailyTaskStrings.length][] ; 
			for (int i = 0; i < DailyTaskStrings.length; i++) {
				String[] DailyTaskStrings2 = DailyTaskStrings[i].split(";"); 
				int[] array = new int[DailyTaskStrings2.length];
				for (int j = 0; j < DailyTaskStrings2.length; j++) {
					int temp = Integer.parseInt(DailyTaskStrings2[j]);	
					array[j] = temp;
				}
				DailyTaskTemp[i] = array;
			}
			DailyTask = DailyTaskTemp ;			
		} else {
			DailyTask = new int[][] {};
		}
		String WeeklyPointString = element.getAttribute("WeeklyPoint"); // 每周任务宝箱积分
		if (WeeklyPointString != null && WeeklyPointString.length() > 0) {
			String[] WeeklyPointStrings = WeeklyPointString.split(";"); 
			int[] WeeklyPointTemp = new int[WeeklyPointStrings.length] ; 
			for (int i = 0; i < WeeklyPointStrings.length; i++) {
				int temp = Integer.parseInt(WeeklyPointStrings[i]);	
				WeeklyPointTemp[i] = temp;
			}
			WeeklyPoint = WeeklyPointTemp ;			
		} else {
			WeeklyPoint = new int[] {};
		}
		String WeeklyTaskString = element.getAttribute("WeeklyTask"); // 每周任务宝箱奖励
		if (WeeklyTaskString != null && WeeklyTaskString.length() > 0) {
			String[] WeeklyTaskStrings = WeeklyTaskString.split("\\|"); 
			int[][] WeeklyTaskTemp = new int[WeeklyTaskStrings.length][] ; 
			for (int i = 0; i < WeeklyTaskStrings.length; i++) {
				String[] WeeklyTaskStrings2 = WeeklyTaskStrings[i].split(";"); 
				int[] array = new int[WeeklyTaskStrings2.length];
				for (int j = 0; j < WeeklyTaskStrings2.length; j++) {
					int temp = Integer.parseInt(WeeklyTaskStrings2[j]);	
					array[j] = temp;
				}
				WeeklyTaskTemp[i] = array;
			}
			WeeklyTask = WeeklyTaskTemp ;			
		} else {
			WeeklyTask = new int[][] {};
		}
		QuickPatrolDuration = Integer.parseInt(element.getAttribute("QuickPatrolDuration") == null || element.getAttribute("QuickPatrolDuration").length() == 0 ? "0"
			: element.getAttribute("QuickPatrolDuration")); // 快速巡逻时长
		MaximumPatrolDuration = Integer.parseInt(element.getAttribute("MaximumPatrolDuration") == null || element.getAttribute("MaximumPatrolDuration").length() == 0 ? "0"
			: element.getAttribute("MaximumPatrolDuration")); // 巡逻时长上限
		QuickPatrolCnt = Integer.parseInt(element.getAttribute("QuickPatrolCnt") == null || element.getAttribute("QuickPatrolCnt").length() == 0 ? "0"
			: element.getAttribute("QuickPatrolCnt")); // 快速巡逻次数
		MonthCardCnt = Integer.parseInt(element.getAttribute("MonthCardCnt") == null || element.getAttribute("MonthCardCnt").length() == 0 ? "0"
			: element.getAttribute("MonthCardCnt")); // 月卡附加次数
		QuickPatrolConsume = Integer.parseInt(element.getAttribute("QuickPatrolConsume") == null || element.getAttribute("QuickPatrolConsume").length() == 0 ? "0"
			: element.getAttribute("QuickPatrolConsume")); // 快速巡逻消耗
		AdPatrolCnt = Integer.parseInt(element.getAttribute("AdPatrolCnt") == null || element.getAttribute("AdPatrolCnt").length() == 0 ? "0"
			: element.getAttribute("AdPatrolCnt")); // 广告巡逻次数
		DayCardCnt = Integer.parseInt(element.getAttribute("DayCardCnt") == null || element.getAttribute("DayCardCnt").length() == 0 ? "0"
			: element.getAttribute("DayCardCnt")); // 日租卡选取数量
	}
	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			for (Element e : list) {
				init(e);
			}
		} catch (Exception e) {
			e.printStackTrace(); 
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}
}
