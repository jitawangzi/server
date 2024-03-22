package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.TextContentConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class TextContentManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(TextContentManager.class);

	private static TextContentManager instance = new TextContentManager();
	private static final String xmlFileName = "TextContent";
	
	private Map<Integer, TextContentConfig> textcontents = new HashMap<>();

	public static TextContentManager getInstance() {
		return instance;
	}

	private TextContentManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public TextContentConfig getTextContentConfig(int id) {
		TextContentConfig config = this.textcontents.get(id);
		if (config == null) { 
			throw new NullPointerException("【TextContent】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public TextContentConfig getTextContentConfigNullable(int id) {
		return this.textcontents.get(id);
	}

	public Collection<TextContentConfig> list() {
		return this.textcontents.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = TextContentManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, TextContentConfig> textcontents = new HashMap<>();
			for (Element e : list) {
				TextContentConfig textcontent = new TextContentConfig(e);
				textcontents.put(textcontent.getId(), textcontent);
			}			

			this.textcontents = com.google.common.collect.ImmutableMap.copyOf(textcontents);

			log.info("load TextContentConfig size[{}]", textcontents.size());

		} catch (Exception e) {
			log.error("load TextContentConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
