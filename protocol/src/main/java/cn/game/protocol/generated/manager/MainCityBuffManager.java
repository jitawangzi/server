package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.MainCityBuffConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class MainCityBuffManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(MainCityBuffManager.class);

	private static MainCityBuffManager instance = new MainCityBuffManager();
	private static final String xmlFileName = "MainCityBuff";
	
	private Map<Integer, MainCityBuffConfig> maincitybuffs = new HashMap<>();

	public static MainCityBuffManager getInstance() {
		return instance;
	}

	private MainCityBuffManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public MainCityBuffConfig getMainCityBuffConfig(int id) {
		MainCityBuffConfig config = this.maincitybuffs.get(id);
		if (config == null) { 
			throw new NullPointerException("【MainCityBuff】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public MainCityBuffConfig getMainCityBuffConfigNullable(int id) {
		return this.maincitybuffs.get(id);
	}

	public Collection<MainCityBuffConfig> list() {
		return this.maincitybuffs.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = MainCityBuffManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, MainCityBuffConfig> map = new HashMap<>();
			for (Element e : list) {
				MainCityBuffConfig maincitybuff = new MainCityBuffConfig(e);
				map.put(maincitybuff.getId(), maincitybuff);
			}
			
			this.maincitybuffs = map;

			log.info("load MainCityBuffConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load MainCityBuffConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
