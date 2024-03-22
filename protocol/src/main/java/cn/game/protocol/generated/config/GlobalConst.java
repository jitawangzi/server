package cn.game.protocol.generated.config;

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

	/** 初始化物品 */
	public static int[][] initItems;		
	/** UID创建取值，参数1版本标识，参数2自增函数起始值 */
	public static int[] CreateUID;		
	/** 初始化战队，英雄 */
	public static int[][] InitialTeam;		
	/** 初始化道具 */
	public static int[][] InitialItem;		
	/** 初始化货币 */
	public static int[][] InitialMoney;		
	/** 体力恢复时间（分钟） */
	public static int StaminaRecoveryTime;		

	static {
		WatchServiceManager.getInstance().register(instance);
	}
	
	public static GlobalConst instance() {
		return instance ; 
	}
	
	public static void init (Element element) throws Exception {	
		String initItemsString = element.getAttribute("initItems"); // 初始化物品
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
		String CreateUIDString = element.getAttribute("CreateUID"); // UID创建取值，参数1版本标识，参数2自增函数起始值
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
		String InitialTeamString = element.getAttribute("InitialTeam"); // 初始化战队，英雄
		if (InitialTeamString != null && InitialTeamString.length() > 0) {
			String[] InitialTeamStrings = InitialTeamString.split("\\|"); 
			int[][] InitialTeamTemp = new int[InitialTeamStrings.length][] ; 
			for (int i = 0; i < InitialTeamStrings.length; i++) {
				String[] InitialTeamStrings2 = InitialTeamStrings[i].split(";"); 
				int[] array = new int[InitialTeamStrings2.length];
				for (int j = 0; j < InitialTeamStrings2.length; j++) {
					int temp = Integer.parseInt(InitialTeamStrings2[j]);	
					array[j] = temp;
				}
				InitialTeamTemp[i] = array;
			}
			InitialTeam = InitialTeamTemp ;			
		} else {
			InitialTeam = new int[][] {};
		}
		String InitialItemString = element.getAttribute("InitialItem"); // 初始化道具
		if (InitialItemString != null && InitialItemString.length() > 0) {
			String[] InitialItemStrings = InitialItemString.split("\\|"); 
			int[][] InitialItemTemp = new int[InitialItemStrings.length][] ; 
			for (int i = 0; i < InitialItemStrings.length; i++) {
				String[] InitialItemStrings2 = InitialItemStrings[i].split(";"); 
				int[] array = new int[InitialItemStrings2.length];
				for (int j = 0; j < InitialItemStrings2.length; j++) {
					int temp = Integer.parseInt(InitialItemStrings2[j]);	
					array[j] = temp;
				}
				InitialItemTemp[i] = array;
			}
			InitialItem = InitialItemTemp ;			
		} else {
			InitialItem = new int[][] {};
		}
		String InitialMoneyString = element.getAttribute("InitialMoney"); // 初始化货币
		if (InitialMoneyString != null && InitialMoneyString.length() > 0) {
			String[] InitialMoneyStrings = InitialMoneyString.split("\\|"); 
			int[][] InitialMoneyTemp = new int[InitialMoneyStrings.length][] ; 
			for (int i = 0; i < InitialMoneyStrings.length; i++) {
				String[] InitialMoneyStrings2 = InitialMoneyStrings[i].split(";"); 
				int[] array = new int[InitialMoneyStrings2.length];
				for (int j = 0; j < InitialMoneyStrings2.length; j++) {
					int temp = Integer.parseInt(InitialMoneyStrings2[j]);	
					array[j] = temp;
				}
				InitialMoneyTemp[i] = array;
			}
			InitialMoney = InitialMoneyTemp ;			
		} else {
			InitialMoney = new int[][] {};
		}
		StaminaRecoveryTime = Integer.parseInt(element.getAttribute("StaminaRecoveryTime") == null || element.getAttribute("StaminaRecoveryTime").length() == 0 ? "0"
			: element.getAttribute("StaminaRecoveryTime")); // 体力恢复时间（分钟）
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
