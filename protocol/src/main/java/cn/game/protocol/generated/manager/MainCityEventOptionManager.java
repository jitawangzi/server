package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.MainCityEventOptionConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class MainCityEventOptionManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(MainCityEventOptionManager.class);

	private static MainCityEventOptionManager instance = new MainCityEventOptionManager();
	private static final String xmlFileName = "MainCityEventOption";
	
	private Map<Integer, MainCityEventOptionConfig> maincityeventoptions = new HashMap<>();

	public static MainCityEventOptionManager getInstance() {
		return instance;
	}

	private MainCityEventOptionManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public MainCityEventOptionConfig getMainCityEventOptionConfig(int id) {
		MainCityEventOptionConfig config = this.maincityeventoptions.get(id);
		if (config == null) { 
			throw new NullPointerException("【MainCityEventOption】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public MainCityEventOptionConfig getMainCityEventOptionConfigNullable(int id) {
		return this.maincityeventoptions.get(id);
	}

	public Collection<MainCityEventOptionConfig> list() {
		return this.maincityeventoptions.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = MainCityEventOptionManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, MainCityEventOptionConfig> map = new HashMap<>();
			for (Element e : list) {
				MainCityEventOptionConfig maincityeventoption = new MainCityEventOptionConfig(e);
				map.put(maincityeventoption.getId(), maincityeventoption);
			}
			
			this.maincityeventoptions = map;

			log.info("load MainCityEventOptionConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load MainCityEventOptionConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
