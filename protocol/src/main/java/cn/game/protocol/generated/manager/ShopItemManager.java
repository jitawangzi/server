package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ShopItemConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class ShopItemManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ShopItemManager.class);

	private static ShopItemManager instance = new ShopItemManager();
	private static final String xmlFileName = "ShopItem";
	
	/** 总数据，按id取值 */
	private Map<Integer, ShopItemConfig> shopitems = new HashMap<>();

	public static ShopItemManager instance() {
		return instance;
	}
	private ShopItemManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public ShopItemConfig get(int id) {
		ShopItemConfig config = this.shopitems.get(id);
		if (config == null) { 
			throw new NullPointerException("【ShopItem】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public ShopItemConfig getNullable(int id) {
		return this.shopitems.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<ShopItemConfig> list() {
		return this.shopitems.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, ShopItemConfig> shopitems = new HashMap<>();
			for (Element e : list) {
				ShopItemConfig shopitem = new ShopItemConfig(e);
				ShopItemConfig old = shopitems.put(shopitem.ID, shopitem);
				if (old != null) {
					throw new IllegalArgumentException("[ShopItemConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.shopitems = com.google.common.collect.ImmutableMap.copyOf(shopitems);

			log.info("load ShopItemConfig size[{}]", shopitems.size());

		} catch (Exception e) {
			throw new RuntimeException("load ShopItemConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
