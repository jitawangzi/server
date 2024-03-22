package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.DrawUpConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class DrawUpManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(DrawUpManager.class);

	private static DrawUpManager instance = new DrawUpManager();
	private static final String xmlFileName = "DrawUp";
	
	private Map<Integer, DrawUpConfig> drawups = new HashMap<>();

	public static DrawUpManager getInstance() {
		return instance;
	}

	private DrawUpManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public DrawUpConfig getDrawUpConfig(int id) {
		DrawUpConfig config = this.drawups.get(id);
		if (config == null) { 
			throw new NullPointerException("【DrawUp】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public DrawUpConfig getDrawUpConfigNullable(int id) {
		return this.drawups.get(id);
	}

	public Collection<DrawUpConfig> list() {
		return this.drawups.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = DrawUpManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, DrawUpConfig> drawups = new HashMap<>();
			for (Element e : list) {
				DrawUpConfig drawup = new DrawUpConfig(e);
				drawups.put(drawup.getId(), drawup);
			}			

			this.drawups = com.google.common.collect.ImmutableMap.copyOf(drawups);

			log.info("load DrawUpConfig size[{}]", drawups.size());

		} catch (Exception e) {
			throw new RuntimeException("load DrawUpConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
