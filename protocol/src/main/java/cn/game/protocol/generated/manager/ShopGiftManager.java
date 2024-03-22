package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ShopGiftConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class ShopGiftManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ShopGiftManager.class);

	private static ShopGiftManager instance = new ShopGiftManager();
	private static final String xmlFileName = "ShopGift";
	
	/** 总数据，按id取值 */
	private Map<Integer, ShopGiftConfig> shopgifts = new HashMap<>();

	public static ShopGiftManager instance() {
		return instance;
	}
	private ShopGiftManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public ShopGiftConfig get(int id) {
		ShopGiftConfig config = this.shopgifts.get(id);
		if (config == null) { 
			throw new NullPointerException("【ShopGift】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public ShopGiftConfig getNullable(int id) {
		return this.shopgifts.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<ShopGiftConfig> list() {
		return this.shopgifts.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, ShopGiftConfig> shopgifts = new HashMap<>();
			for (Element e : list) {
				ShopGiftConfig shopgift = new ShopGiftConfig(e);
				ShopGiftConfig old = shopgifts.put(shopgift.ID, shopgift);
				if (old != null) {
					throw new IllegalArgumentException("[ShopGiftConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.shopgifts = com.google.common.collect.ImmutableMap.copyOf(shopgifts);

			log.info("load ShopGiftConfig size[{}]", shopgifts.size());

		} catch (Exception e) {
			throw new RuntimeException("load ShopGiftConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
