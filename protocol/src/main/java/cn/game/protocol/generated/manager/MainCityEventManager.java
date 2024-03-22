package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.MainCityEventConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class MainCityEventManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(MainCityEventManager.class);

	private static MainCityEventManager instance = new MainCityEventManager();
	private static final String xmlFileName = "MainCityEvent";
	
	private Map<Integer, MainCityEventConfig> maincityevents = new HashMap<>();
	private Map<Integer,List<MainCityEventConfig>> types = new HashMap<>();

	public static MainCityEventManager getInstance() {
		return instance;
	}

	private MainCityEventManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public MainCityEventConfig getMainCityEventConfig(int id) {
		MainCityEventConfig config = this.maincityevents.get(id);
		if (config == null) { 
			throw new NullPointerException("【MainCityEvent】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public MainCityEventConfig getMainCityEventConfigNullable(int id) {
		return this.maincityevents.get(id);
	}

	public List<MainCityEventConfig> getTypeList(int type) {
		return this.types.get(type);
	}
	public Collection<MainCityEventConfig> list() {
		return this.maincityevents.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = MainCityEventManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, MainCityEventConfig> map = new HashMap<>();
			Map<Integer, List<MainCityEventConfig>> typeMap = new HashMap<>();
			for (Element e : list) {
				MainCityEventConfig maincityevent = new MainCityEventConfig(e);
				List<MainCityEventConfig> typeList = typeMap.get(maincityevent.getType()); 
				if (typeList == null){
					typeList = new ArrayList<MainCityEventConfig>(2) ; 
					typeMap.put(maincityevent.getType() ,typeList) ; 
				}
				typeList.add(maincityevent) ;
				map.put(maincityevent.getId(), maincityevent);
			}
			
			this.types = typeMap;
			this.maincityevents = map;

			log.info("load MainCityEventConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load MainCityEventConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
