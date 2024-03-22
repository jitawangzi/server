package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.RoleGiftCardsConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RoleGiftCardsManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RoleGiftCardsManager.class);

	private static RoleGiftCardsManager instance = new RoleGiftCardsManager();
	private static final String xmlFileName = "RoleGiftCards";
	
	private Map<Integer, RoleGiftCardsConfig> rolegiftcardss = new HashMap<>();

	public static RoleGiftCardsManager getInstance() {
		return instance;
	}

	private RoleGiftCardsManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public RoleGiftCardsConfig getRoleGiftCardsConfig(int id) {
		RoleGiftCardsConfig config = this.rolegiftcardss.get(id);
		if (config == null) { 
			throw new NullPointerException("【RoleGiftCards】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public RoleGiftCardsConfig getRoleGiftCardsConfigNullable(int id) {
		return this.rolegiftcardss.get(id);
	}

	public Collection<RoleGiftCardsConfig> list() {
		return this.rolegiftcardss.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = RoleGiftCardsManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, RoleGiftCardsConfig> rolegiftcardss = new HashMap<>();
			for (Element e : list) {
				RoleGiftCardsConfig rolegiftcards = new RoleGiftCardsConfig(e);
				rolegiftcardss.put(rolegiftcards.getId(), rolegiftcards);
			}			

			this.rolegiftcardss = com.google.common.collect.ImmutableMap.copyOf(rolegiftcardss);

			log.info("load RoleGiftCardsConfig size[{}]", rolegiftcardss.size());

		} catch (Exception e) {
			throw new RuntimeException("load RoleGiftCardsConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
