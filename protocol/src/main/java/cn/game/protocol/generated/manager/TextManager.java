package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.TextConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class TextManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(TextManager.class);

	private static TextManager instance = new TextManager();
	private static final String xmlFileName = "Text";
	
	/** 总数据，按id取值 */
	private Map<Integer, TextConfig> texts = new HashMap<>();

	public static TextManager instance() {
		return instance;
	}
	private TextManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public TextConfig get(int id) {
		TextConfig config = this.texts.get(id);
		if (config == null) { 
			throw new NullPointerException("【Text】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public TextConfig getNullable(int id) {
		return this.texts.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<TextConfig> list() {
		return this.texts.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, TextConfig> texts = new HashMap<>();
			for (Element e : list) {
				TextConfig text = new TextConfig(e);
				TextConfig old = texts.put(text.ID, text);
				if (old != null) {
					throw new IllegalArgumentException("[TextConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.texts = com.google.common.collect.ImmutableMap.copyOf(texts);

			log.info("load TextConfig size[{}]", texts.size());

		} catch (Exception e) {
			throw new RuntimeException("load TextConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
