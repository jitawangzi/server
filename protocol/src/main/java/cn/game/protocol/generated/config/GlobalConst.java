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
	/** 【肉鸽】全场AOE类肉鸽规定 */
	public static int[] Rogueroll1;		
	/** 【肉鸽】全场纯加属性类肉鸽规定 */
	public static int[] Rogueroll2;		

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
