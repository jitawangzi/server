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
	/** 【挂机】快速巡逻时长 */
	public static int QuickPatrolDuration;		
	/** 【挂机】巡逻时长上限 */
	public static int MaximumPatrolDuration;		
	/** 【挂机】快速巡逻次数 */
	public static int QuickPatrolCnt;		
	/** 【挂机】月卡附加次数 */
	public static int MonthCardCnt;		
	/** 【挂机】快速巡逻消耗 */
	public static int QuickPatrolConsume;		
	/** 【挂机】广告巡逻次数 */
	public static int AdPatrolCnt;		
	/** 【黑市】日租卡选取数量 */
	public static int DayCardCnt;		
	/** 【黑市】黑市格子数量 */
	public static int HeishiShelvesCnt;		
	/** 【黑市】黑市刷新 */
	public static int HeishiFreeRefresh;		
	/** 【请神】高级抽卡 */
	public static int[] AdvancedCardDraw;		
	/** 【请神】至尊抽卡 */
	public static int[] UltimateCardDraw;		
	/** 【请神】高级抽卡神将显示组 */
	public static int[][] AdvancedCardDrawGroup;		
	/** 【请神】至尊抽卡神将显示组 */
	public static int[][] UltimateCardDrawGroup;		
	/** 【请神】第1次必抽-掉落id */
	public static int FirstMandatoryDraw;		
	/** 【妖王别跑】速战次数 */
	public static int DemonKingCnt;		
	/** 【妖王别跑】速战消耗 */
	public static int[][] DemonKingConsume;		
	/** 【道心磨砺】免费次数 */
	public static int DaoHeartFreeCnt;		
	/** 【道心磨砺】付费次数 */
	public static int DaoHeartPayCnt;		
	/** 【道心磨砺】购买消耗 */
	public static int[][] DaoHeartConsume;		
	/** 【月卡】双月卡奖励 */
	public static int[][] DoubleBonus;		

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
		QuickPatrolDuration = Integer.parseInt(element.getAttribute("QuickPatrolDuration") == null || element.getAttribute("QuickPatrolDuration").length() == 0 ? "0"
			: element.getAttribute("QuickPatrolDuration")); // 【挂机】快速巡逻时长
		MaximumPatrolDuration = Integer.parseInt(element.getAttribute("MaximumPatrolDuration") == null || element.getAttribute("MaximumPatrolDuration").length() == 0 ? "0"
			: element.getAttribute("MaximumPatrolDuration")); // 【挂机】巡逻时长上限
		QuickPatrolCnt = Integer.parseInt(element.getAttribute("QuickPatrolCnt") == null || element.getAttribute("QuickPatrolCnt").length() == 0 ? "0"
			: element.getAttribute("QuickPatrolCnt")); // 【挂机】快速巡逻次数
		MonthCardCnt = Integer.parseInt(element.getAttribute("MonthCardCnt") == null || element.getAttribute("MonthCardCnt").length() == 0 ? "0"
			: element.getAttribute("MonthCardCnt")); // 【挂机】月卡附加次数
		QuickPatrolConsume = Integer.parseInt(element.getAttribute("QuickPatrolConsume") == null || element.getAttribute("QuickPatrolConsume").length() == 0 ? "0"
			: element.getAttribute("QuickPatrolConsume")); // 【挂机】快速巡逻消耗
		AdPatrolCnt = Integer.parseInt(element.getAttribute("AdPatrolCnt") == null || element.getAttribute("AdPatrolCnt").length() == 0 ? "0"
			: element.getAttribute("AdPatrolCnt")); // 【挂机】广告巡逻次数
		DayCardCnt = Integer.parseInt(element.getAttribute("DayCardCnt") == null || element.getAttribute("DayCardCnt").length() == 0 ? "0"
			: element.getAttribute("DayCardCnt")); // 【黑市】日租卡选取数量
		HeishiShelvesCnt = Integer.parseInt(element.getAttribute("HeishiShelvesCnt") == null || element.getAttribute("HeishiShelvesCnt").length() == 0 ? "0"
			: element.getAttribute("HeishiShelvesCnt")); // 【黑市】黑市格子数量
		HeishiFreeRefresh = Integer.parseInt(element.getAttribute("HeishiFreeRefresh") == null || element.getAttribute("HeishiFreeRefresh").length() == 0 ? "0"
			: element.getAttribute("HeishiFreeRefresh")); // 【黑市】黑市刷新
		String AdvancedCardDrawString = element.getAttribute("AdvancedCardDraw"); // 【请神】高级抽卡
		if (AdvancedCardDrawString != null && AdvancedCardDrawString.length() > 0) {
			String[] AdvancedCardDrawStrings = AdvancedCardDrawString.split(";"); 
			int[] AdvancedCardDrawTemp = new int[AdvancedCardDrawStrings.length] ; 
			for (int i = 0; i < AdvancedCardDrawStrings.length; i++) {
				int temp = Integer.parseInt(AdvancedCardDrawStrings[i]);	
				AdvancedCardDrawTemp[i] = temp;
			}
			AdvancedCardDraw = AdvancedCardDrawTemp ;			
		} else {
			AdvancedCardDraw = new int[] {};
		}
		String UltimateCardDrawString = element.getAttribute("UltimateCardDraw"); // 【请神】至尊抽卡
		if (UltimateCardDrawString != null && UltimateCardDrawString.length() > 0) {
			String[] UltimateCardDrawStrings = UltimateCardDrawString.split(";"); 
			int[] UltimateCardDrawTemp = new int[UltimateCardDrawStrings.length] ; 
			for (int i = 0; i < UltimateCardDrawStrings.length; i++) {
				int temp = Integer.parseInt(UltimateCardDrawStrings[i]);	
				UltimateCardDrawTemp[i] = temp;
			}
			UltimateCardDraw = UltimateCardDrawTemp ;			
		} else {
			UltimateCardDraw = new int[] {};
		}
		String AdvancedCardDrawGroupString = element.getAttribute("AdvancedCardDrawGroup"); // 【请神】高级抽卡神将显示组
		if (AdvancedCardDrawGroupString != null && AdvancedCardDrawGroupString.length() > 0) {
			String[] AdvancedCardDrawGroupStrings = AdvancedCardDrawGroupString.split("\\|"); 
			int[][] AdvancedCardDrawGroupTemp = new int[AdvancedCardDrawGroupStrings.length][] ; 
			for (int i = 0; i < AdvancedCardDrawGroupStrings.length; i++) {
				String[] AdvancedCardDrawGroupStrings2 = AdvancedCardDrawGroupStrings[i].split(";"); 
				int[] array = new int[AdvancedCardDrawGroupStrings2.length];
				for (int j = 0; j < AdvancedCardDrawGroupStrings2.length; j++) {
					int temp = Integer.parseInt(AdvancedCardDrawGroupStrings2[j]);	
					array[j] = temp;
				}
				AdvancedCardDrawGroupTemp[i] = array;
			}
			AdvancedCardDrawGroup = AdvancedCardDrawGroupTemp ;			
		} else {
			AdvancedCardDrawGroup = new int[][] {};
		}
		String UltimateCardDrawGroupString = element.getAttribute("UltimateCardDrawGroup"); // 【请神】至尊抽卡神将显示组
		if (UltimateCardDrawGroupString != null && UltimateCardDrawGroupString.length() > 0) {
			String[] UltimateCardDrawGroupStrings = UltimateCardDrawGroupString.split("\\|"); 
			int[][] UltimateCardDrawGroupTemp = new int[UltimateCardDrawGroupStrings.length][] ; 
			for (int i = 0; i < UltimateCardDrawGroupStrings.length; i++) {
				String[] UltimateCardDrawGroupStrings2 = UltimateCardDrawGroupStrings[i].split(";"); 
				int[] array = new int[UltimateCardDrawGroupStrings2.length];
				for (int j = 0; j < UltimateCardDrawGroupStrings2.length; j++) {
					int temp = Integer.parseInt(UltimateCardDrawGroupStrings2[j]);	
					array[j] = temp;
				}
				UltimateCardDrawGroupTemp[i] = array;
			}
			UltimateCardDrawGroup = UltimateCardDrawGroupTemp ;			
		} else {
			UltimateCardDrawGroup = new int[][] {};
		}
		FirstMandatoryDraw = Integer.parseInt(element.getAttribute("FirstMandatoryDraw") == null || element.getAttribute("FirstMandatoryDraw").length() == 0 ? "0"
			: element.getAttribute("FirstMandatoryDraw")); // 【请神】第1次必抽-掉落id
		DemonKingCnt = Integer.parseInt(element.getAttribute("DemonKingCnt") == null || element.getAttribute("DemonKingCnt").length() == 0 ? "0"
			: element.getAttribute("DemonKingCnt")); // 【妖王别跑】速战次数
		String DemonKingConsumeString = element.getAttribute("DemonKingConsume"); // 【妖王别跑】速战消耗
		if (DemonKingConsumeString != null && DemonKingConsumeString.length() > 0) {
			String[] DemonKingConsumeStrings = DemonKingConsumeString.split("\\|"); 
			int[][] DemonKingConsumeTemp = new int[DemonKingConsumeStrings.length][] ; 
			for (int i = 0; i < DemonKingConsumeStrings.length; i++) {
				String[] DemonKingConsumeStrings2 = DemonKingConsumeStrings[i].split(";"); 
				int[] array = new int[DemonKingConsumeStrings2.length];
				for (int j = 0; j < DemonKingConsumeStrings2.length; j++) {
					int temp = Integer.parseInt(DemonKingConsumeStrings2[j]);	
					array[j] = temp;
				}
				DemonKingConsumeTemp[i] = array;
			}
			DemonKingConsume = DemonKingConsumeTemp ;			
		} else {
			DemonKingConsume = new int[][] {};
		}
		DaoHeartFreeCnt = Integer.parseInt(element.getAttribute("DaoHeartFreeCnt") == null || element.getAttribute("DaoHeartFreeCnt").length() == 0 ? "0"
			: element.getAttribute("DaoHeartFreeCnt")); // 【道心磨砺】免费次数
		DaoHeartPayCnt = Integer.parseInt(element.getAttribute("DaoHeartPayCnt") == null || element.getAttribute("DaoHeartPayCnt").length() == 0 ? "0"
			: element.getAttribute("DaoHeartPayCnt")); // 【道心磨砺】付费次数
		String DaoHeartConsumeString = element.getAttribute("DaoHeartConsume"); // 【道心磨砺】购买消耗
		if (DaoHeartConsumeString != null && DaoHeartConsumeString.length() > 0) {
			String[] DaoHeartConsumeStrings = DaoHeartConsumeString.split("\\|"); 
			int[][] DaoHeartConsumeTemp = new int[DaoHeartConsumeStrings.length][] ; 
			for (int i = 0; i < DaoHeartConsumeStrings.length; i++) {
				String[] DaoHeartConsumeStrings2 = DaoHeartConsumeStrings[i].split(";"); 
				int[] array = new int[DaoHeartConsumeStrings2.length];
				for (int j = 0; j < DaoHeartConsumeStrings2.length; j++) {
					int temp = Integer.parseInt(DaoHeartConsumeStrings2[j]);	
					array[j] = temp;
				}
				DaoHeartConsumeTemp[i] = array;
			}
			DaoHeartConsume = DaoHeartConsumeTemp ;			
		} else {
			DaoHeartConsume = new int[][] {};
		}
		String DoubleBonusString = element.getAttribute("DoubleBonus"); // 【月卡】双月卡奖励
		if (DoubleBonusString != null && DoubleBonusString.length() > 0) {
			String[] DoubleBonusStrings = DoubleBonusString.split("\\|"); 
			int[][] DoubleBonusTemp = new int[DoubleBonusStrings.length][] ; 
			for (int i = 0; i < DoubleBonusStrings.length; i++) {
				String[] DoubleBonusStrings2 = DoubleBonusStrings[i].split(";"); 
				int[] array = new int[DoubleBonusStrings2.length];
				for (int j = 0; j < DoubleBonusStrings2.length; j++) {
					int temp = Integer.parseInt(DoubleBonusStrings2[j]);	
					array[j] = temp;
				}
				DoubleBonusTemp[i] = array;
			}
			DoubleBonus = DoubleBonusTemp ;			
		} else {
			DoubleBonus = new int[][] {};
		}
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
