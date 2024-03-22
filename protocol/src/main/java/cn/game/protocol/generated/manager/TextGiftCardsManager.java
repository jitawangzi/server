package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.TextGiftCardsConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class TextGiftCardsManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(TextGiftCardsManager.class);

	private static TextGiftCardsManager instance = new TextGiftCardsManager();
	private static final String xmlFileName = "TextGiftCards";
	
	private Map<Integer, TextGiftCardsConfig> textgiftcardss = new HashMap<>();

	public static TextGiftCardsManager getInstance() {
		return instance;
	}

	private TextGiftCardsManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public TextGiftCardsConfig getTextGiftCardsConfig(int id) {
		TextGiftCardsConfig config = this.textgiftcardss.get(id);
		if (config == null) { 
			throw new NullPointerException("【TextGiftCards】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public TextGiftCardsConfig getTextGiftCardsConfigNullable(int id) {
		return this.textgiftcardss.get(id);
	}

	public Collection<TextGiftCardsConfig> list() {
		return this.textgiftcardss.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = TextGiftCardsManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, TextGiftCardsConfig> textgiftcardss = new HashMap<>();
			for (Element e : list) {
				TextGiftCardsConfig textgiftcards = new TextGiftCardsConfig(e);
				textgiftcardss.put(textgiftcards.getId(), textgiftcards);
			}			

			this.textgiftcardss = com.google.common.collect.ImmutableMap.copyOf(textgiftcardss);

			log.info("load TextGiftCardsConfig size[{}]", textgiftcardss.size());

		} catch (Exception e) {
			throw new RuntimeException("load TextGiftCardsConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
