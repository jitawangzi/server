package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ItemConsumablesConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class ItemConsumablesManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ItemConsumablesManager.class);

	private static ItemConsumablesManager instance = new ItemConsumablesManager();
	private static final String xmlFileName = "ItemConsumables";
	
	private Map<Integer, ItemConsumablesConfig> itemconsumabless = new HashMap<>();

	public static ItemConsumablesManager getInstance() {
		return instance;
	}

	private ItemConsumablesManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public ItemConsumablesConfig getItemConsumablesConfig(int id) {
		ItemConsumablesConfig config = this.itemconsumabless.get(id);
		if (config == null) { 
			throw new NullPointerException("【ItemConsumables】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public ItemConsumablesConfig getItemConsumablesConfigNullable(int id) {
		return this.itemconsumabless.get(id);
	}

	public Collection<ItemConsumablesConfig> list() {
		return this.itemconsumabless.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = ItemConsumablesManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, ItemConsumablesConfig> itemconsumabless = new HashMap<>();
			for (Element e : list) {
				ItemConsumablesConfig itemconsumables = new ItemConsumablesConfig(e);
				itemconsumabless.put(itemconsumables.getId(), itemconsumables);
			}			

			this.itemconsumabless = com.google.common.collect.ImmutableMap.copyOf(itemconsumabless);

			log.info("load ItemConsumablesConfig size[{}]", itemconsumabless.size());

		} catch (Exception e) {
			throw new RuntimeException("load ItemConsumablesConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
