package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ShopItemGroupConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class ShopItemGroupManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ShopItemGroupManager.class);

	private static ShopItemGroupManager instance = new ShopItemGroupManager();
	private static final String xmlFileName = "ShopItemGroup";
	
	/** 总数据，按id取值 */
	private Map<Integer, ShopItemGroupConfig> shopitemgroups = new HashMap<>();

	public static ShopItemGroupManager instance() {
		return instance;
	}
	private ShopItemGroupManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public ShopItemGroupConfig get(int id) {
		ShopItemGroupConfig config = this.shopitemgroups.get(id);
		if (config == null) { 
			throw new NullPointerException("【ShopItemGroup】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public ShopItemGroupConfig getNullable(int id) {
		return this.shopitemgroups.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<ShopItemGroupConfig> list() {
		return this.shopitemgroups.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, ShopItemGroupConfig> shopitemgroups = new HashMap<>();
			for (Element e : list) {
				ShopItemGroupConfig shopitemgroup = new ShopItemGroupConfig(e);
				ShopItemGroupConfig old = shopitemgroups.put(shopitemgroup.ID, shopitemgroup);
				if (old != null) {
					throw new IllegalArgumentException("[ShopItemGroupConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.shopitemgroups = com.google.common.collect.ImmutableMap.copyOf(shopitemgroups);

			log.info("load ShopItemGroupConfig size[{}]", shopitemgroups.size());

		} catch (Exception e) {
			throw new RuntimeException("load ShopItemGroupConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
