package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.StoreRecommendConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class StoreRecommendManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(StoreRecommendManager.class);

	private static StoreRecommendManager instance = new StoreRecommendManager();
	private static final String xmlFileName = "StoreRecommend";
	
	private Map<Integer, StoreRecommendConfig> storerecommends = new HashMap<>();

	public static StoreRecommendManager getInstance() {
		return instance;
	}

	private StoreRecommendManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public StoreRecommendConfig getStoreRecommendConfig(int id) {
		StoreRecommendConfig config = this.storerecommends.get(id);
		if (config == null) { 
			throw new NullPointerException("【StoreRecommend】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public StoreRecommendConfig getStoreRecommendConfigNullable(int id) {
		return this.storerecommends.get(id);
	}

	public Collection<StoreRecommendConfig> list() {
		return this.storerecommends.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = StoreRecommendManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, StoreRecommendConfig> storerecommends = new HashMap<>();
			for (Element e : list) {
				StoreRecommendConfig storerecommend = new StoreRecommendConfig(e);
				storerecommends.put(storerecommend.getId(), storerecommend);
			}			

			this.storerecommends = com.google.common.collect.ImmutableMap.copyOf(storerecommends);

			log.info("load StoreRecommendConfig size[{}]", storerecommends.size());

		} catch (Exception e) {
			throw new RuntimeException("load StoreRecommendConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
