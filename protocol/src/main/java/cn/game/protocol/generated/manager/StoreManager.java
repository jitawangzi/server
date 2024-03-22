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

import cn.game.protocol.generated.config.StoreConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class StoreManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(StoreManager.class);

	private static StoreManager instance = new StoreManager();
	private static final String xmlFileName = "Store";
	
	private Map<Integer, StoreConfig> stores = new HashMap<>();
	private Map<Integer,List<StoreConfig>> firstTabTypes = new HashMap<>();

	public static StoreManager getInstance() {
		return instance;
	}

	private StoreManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public StoreConfig getStoreConfig(int id) {
		StoreConfig config = this.stores.get(id);
		if (config == null) { 
			throw new NullPointerException("【Store】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public StoreConfig getStoreConfigNullable(int id) {
		return this.stores.get(id);
	}

	public List<StoreConfig> getFirstTabTypeList(int firstTabType) {
		return this.firstTabTypes.get(firstTabType);
	}
	public Collection<StoreConfig> list() {
		return this.stores.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = StoreManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, StoreConfig> stores = new HashMap<>();
			Map<Integer, List<StoreConfig>> firstTabTypes = new HashMap<>();
			for (Element e : list) {
				StoreConfig store = new StoreConfig(e);
				List<StoreConfig> firstTabTypeList = firstTabTypes.get(store.getFirstTabType()); 
				if (firstTabTypeList == null){
					firstTabTypeList = new ArrayList<StoreConfig>(2) ; 
					firstTabTypes.put(store.getFirstTabType() ,firstTabTypeList) ; 
				}
				firstTabTypeList.add(store) ;
				stores.put(store.getId(), store);
			}			

			this.firstTabTypes = com.google.common.collect.ImmutableMap.copyOf(firstTabTypes);			
			this.stores = com.google.common.collect.ImmutableMap.copyOf(stores);

			log.info("load StoreConfig size[{}]", stores.size());

		} catch (Exception e) {
			throw new RuntimeException("load StoreConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
