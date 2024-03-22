package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ShopConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class ShopManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ShopManager.class);

	private static ShopManager instance = new ShopManager();
	private static final String xmlFileName = "Shop";
	
	/** 总数据，按id取值 */
	private Map<Integer, ShopConfig> shops = new HashMap<>();

	public static ShopManager instance() {
		return instance;
	}
	private ShopManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public ShopConfig get(int id) {
		ShopConfig config = this.shops.get(id);
		if (config == null) { 
			throw new NullPointerException("【Shop】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public ShopConfig getNullable(int id) {
		return this.shops.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<ShopConfig> list() {
		return this.shops.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, ShopConfig> shops = new HashMap<>();
			for (Element e : list) {
				ShopConfig shop = new ShopConfig(e);
				ShopConfig old = shops.put(shop.ID, shop);
				if (old != null) {
					throw new IllegalArgumentException("[ShopConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.shops = com.google.common.collect.ImmutableMap.copyOf(shops);

			log.info("load ShopConfig size[{}]", shops.size());

		} catch (Exception e) {
			throw new RuntimeException("load ShopConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
