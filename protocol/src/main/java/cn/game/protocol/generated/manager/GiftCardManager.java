package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.GiftCardConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class GiftCardManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(GiftCardManager.class);

	private static GiftCardManager instance = new GiftCardManager();
	private static final String xmlFileName = "GiftCard";
	
	private Map<Integer, GiftCardConfig> giftcards = new HashMap<>();

	public static GiftCardManager getInstance() {
		return instance;
	}

	private GiftCardManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public GiftCardConfig getGiftCardConfig(int id) {
		GiftCardConfig config = this.giftcards.get(id);
		if (config == null) { 
			throw new NullPointerException("【GiftCard】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public GiftCardConfig getGiftCardConfigNullable(int id) {
		return this.giftcards.get(id);
	}

	public Collection<GiftCardConfig> list() {
		return this.giftcards.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = GiftCardManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, GiftCardConfig> map = new HashMap<>();
			for (Element e : list) {
				GiftCardConfig giftcard = new GiftCardConfig(e);
				map.put(giftcard.getId(), giftcard);
			}
			
			this.giftcards = map;

			log.info("load GiftCardConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load GiftCardConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
