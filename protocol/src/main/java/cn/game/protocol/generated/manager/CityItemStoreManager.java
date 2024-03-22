package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.CityItemStoreConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class CityItemStoreManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(CityItemStoreManager.class);

	private static CityItemStoreManager instance = new CityItemStoreManager();
	private static final String xmlFileName = "CityItemStore";
	
	private Map<Integer, CityItemStoreConfig> cityitemstores = new HashMap<>();

	public static CityItemStoreManager getInstance() {
		return instance;
	}

	private CityItemStoreManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public CityItemStoreConfig getCityItemStoreConfig(int id) {
		CityItemStoreConfig config = this.cityitemstores.get(id);
		if (config == null) { 
			throw new NullPointerException("【CityItemStore】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public CityItemStoreConfig getCityItemStoreConfigNullable(int id) {
		return this.cityitemstores.get(id);
	}

	public Collection<CityItemStoreConfig> list() {
		return this.cityitemstores.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = CityItemStoreManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, CityItemStoreConfig> cityitemstores = new HashMap<>();
			for (Element e : list) {
				CityItemStoreConfig cityitemstore = new CityItemStoreConfig(e);
				cityitemstores.put(cityitemstore.getId(), cityitemstore);
			}			

			this.cityitemstores = com.google.common.collect.ImmutableMap.copyOf(cityitemstores);

			log.info("load CityItemStoreConfig size[{}]", cityitemstores.size());

		} catch (Exception e) {
			throw new RuntimeException("load CityItemStoreConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
