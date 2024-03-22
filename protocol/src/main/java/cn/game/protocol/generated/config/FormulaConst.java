package cn.game.protocol.generated.config;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.commons.lang.StringUtils;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 公式参数
 * 
 * 工具生成的，不要手动修改
 */
public class FormulaConst extends ResourceListener {

	private static final String xmlFileName = "FormulaConst";
	private static FormulaConst instance = new FormulaConst();

	/** 账号初始角色 */
	public static int heroInit;		
	/** 角色突破最大星数 */
	public static int roleStarMax;		
	/** 角色最大等级 */
	public static int roleLevelMax;		
	/** 核心突破最大等级 */
	public static int coreBreakLevelMax;		

	static {
		WatchServiceManager.getInstance().register(instance);
	}
	
	public static FormulaConst getInstance() {
		return instance ; 
	}
	
	public static void init (Element element) {	

		heroInit = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("heroInit")) ? "0"
			: element.getAttribute("heroInit")); 

		roleStarMax = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("roleStarMax")) ? "0"
			: element.getAttribute("roleStarMax")); 

		roleLevelMax = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("roleLevelMax")) ? "0"
			: element.getAttribute("roleLevelMax")); 

		coreBreakLevelMax = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("coreBreakLevelMax")) ? "0"
			: element.getAttribute("coreBreakLevelMax")); 
	}
	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = FormulaConst.class.getClassLoader();
			}
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
