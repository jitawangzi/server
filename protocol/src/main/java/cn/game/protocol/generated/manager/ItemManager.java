package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ItemConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class ItemManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ItemManager.class);

	private static ItemManager instance = new ItemManager();
	private static final String xmlFileName = "Item";
	
	/** 总数据，按id取值 */
	private Map<Integer, ItemConfig> items = new HashMap<>();

	public static ItemManager instance() {
		return instance;
	}
	private ItemManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public ItemConfig get(int id) {
		ItemConfig config = this.items.get(id);
		if (config == null) { 
			throw new NullPointerException("【Item】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public ItemConfig getNullable(int id) {
		return this.items.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<ItemConfig> list() {
		return this.items.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, ItemConfig> items = new HashMap<>();
			for (Element e : list) {
				ItemConfig item = new ItemConfig(e);
				ItemConfig old = items.put(item.ID, item);
				if (old != null) {
					throw new IllegalArgumentException("[ItemConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.items = com.google.common.collect.ImmutableMap.copyOf(items);

			log.info("load ItemConfig size[{}]", items.size());

		} catch (Exception e) {
			throw new RuntimeException("load ItemConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
