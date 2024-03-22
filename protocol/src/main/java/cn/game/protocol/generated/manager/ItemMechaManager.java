package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ItemMechaConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class ItemMechaManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ItemMechaManager.class);

	private static ItemMechaManager instance = new ItemMechaManager();
	private static final String xmlFileName = "ItemMecha";
	
	private Map<Integer, ItemMechaConfig> itemmechas = new HashMap<>();

	public static ItemMechaManager getInstance() {
		return instance;
	}

	private ItemMechaManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public ItemMechaConfig getItemMechaConfig(int id) {
		ItemMechaConfig config = this.itemmechas.get(id);
		if (config == null) { 
			throw new NullPointerException("【ItemMecha】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public ItemMechaConfig getItemMechaConfigNullable(int id) {
		return this.itemmechas.get(id);
	}

	public Collection<ItemMechaConfig> list() {
		return this.itemmechas.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = ItemMechaManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, ItemMechaConfig> itemmechas = new HashMap<>();
			for (Element e : list) {
				ItemMechaConfig itemmecha = new ItemMechaConfig(e);
				itemmechas.put(itemmecha.getId(), itemmecha);
			}			

			this.itemmechas = com.google.common.collect.ImmutableMap.copyOf(itemmechas);

			log.info("load ItemMechaConfig size[{}]", itemmechas.size());

		} catch (Exception e) {
			throw new RuntimeException("load ItemMechaConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
