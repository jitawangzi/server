package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.StoreRechargeConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class StoreRechargeManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(StoreRechargeManager.class);

	private static StoreRechargeManager instance = new StoreRechargeManager();
	private static final String xmlFileName = "StoreRecharge";
	
	private Map<Integer, StoreRechargeConfig> storerecharges = new HashMap<>();

	public static StoreRechargeManager getInstance() {
		return instance;
	}

	private StoreRechargeManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public StoreRechargeConfig getStoreRechargeConfig(int id) {
		StoreRechargeConfig config = this.storerecharges.get(id);
		if (config == null) { 
			throw new NullPointerException("【StoreRecharge】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public StoreRechargeConfig getStoreRechargeConfigNullable(int id) {
		return this.storerecharges.get(id);
	}

	public Collection<StoreRechargeConfig> list() {
		return this.storerecharges.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = StoreRechargeManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, StoreRechargeConfig> storerecharges = new HashMap<>();
			for (Element e : list) {
				StoreRechargeConfig storerecharge = new StoreRechargeConfig(e);
				storerecharges.put(storerecharge.getId(), storerecharge);
			}			

			this.storerecharges = com.google.common.collect.ImmutableMap.copyOf(storerecharges);

			log.info("load StoreRechargeConfig size[{}]", storerecharges.size());

		} catch (Exception e) {
			throw new RuntimeException("load StoreRechargeConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
