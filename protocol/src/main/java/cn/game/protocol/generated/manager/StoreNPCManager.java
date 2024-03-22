package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.StoreNPCConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class StoreNPCManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(StoreNPCManager.class);

	private static StoreNPCManager instance = new StoreNPCManager();
	private static final String xmlFileName = "StoreNPC";
	
	private Map<Integer, StoreNPCConfig> storenpcs = new HashMap<>();

	public static StoreNPCManager getInstance() {
		return instance;
	}

	private StoreNPCManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public StoreNPCConfig getStoreNPCConfig(int id) {
		StoreNPCConfig config = this.storenpcs.get(id);
		if (config == null) { 
			throw new NullPointerException("【StoreNPC】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public StoreNPCConfig getStoreNPCConfigNullable(int id) {
		return this.storenpcs.get(id);
	}

	public Collection<StoreNPCConfig> list() {
		return this.storenpcs.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = StoreNPCManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, StoreNPCConfig> storenpcs = new HashMap<>();
			for (Element e : list) {
				StoreNPCConfig storenpc = new StoreNPCConfig(e);
				storenpcs.put(storenpc.getId(), storenpc);
			}			

			this.storenpcs = com.google.common.collect.ImmutableMap.copyOf(storenpcs);

			log.info("load StoreNPCConfig size[{}]", storenpcs.size());

		} catch (Exception e) {
			throw new RuntimeException("load StoreNPCConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
