package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.RoleGiftCardConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RoleGiftCardManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RoleGiftCardManager.class);

	private static RoleGiftCardManager instance = new RoleGiftCardManager();
	private static final String xmlFileName = "RoleGiftCard";
	
	private Map<Integer, RoleGiftCardConfig> rolegiftcards = new HashMap<>();

	public static RoleGiftCardManager getInstance() {
		return instance;
	}

	private RoleGiftCardManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public RoleGiftCardConfig getRoleGiftCardConfig(int id) {
		RoleGiftCardConfig config = this.rolegiftcards.get(id);
		if (config == null) { 
			throw new NullPointerException("【RoleGiftCard】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public RoleGiftCardConfig getRoleGiftCardConfigNullable(int id) {
		return this.rolegiftcards.get(id);
	}

	public Collection<RoleGiftCardConfig> list() {
		return this.rolegiftcards.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = RoleGiftCardManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, RoleGiftCardConfig> map = new HashMap<>();
			for (Element e : list) {
				RoleGiftCardConfig rolegiftcard = new RoleGiftCardConfig(e);
				map.put(rolegiftcard.getId(), rolegiftcard);
			}
			
			this.rolegiftcards = map;

			log.info("load RoleGiftCardConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load RoleGiftCardConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
