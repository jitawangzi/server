package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.DrawRountineConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class DrawRountineManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(DrawRountineManager.class);

	private static DrawRountineManager instance = new DrawRountineManager();
	private static final String xmlFileName = "DrawRountine";
	
	private Map<Integer, DrawRountineConfig> drawrountines = new HashMap<>();

	public static DrawRountineManager getInstance() {
		return instance;
	}

	private DrawRountineManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public DrawRountineConfig getDrawRountineConfig(int id) {
		DrawRountineConfig config = this.drawrountines.get(id);
		if (config == null) { 
			throw new NullPointerException("【DrawRountine】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public DrawRountineConfig getDrawRountineConfigNullable(int id) {
		return this.drawrountines.get(id);
	}

	public Collection<DrawRountineConfig> list() {
		return this.drawrountines.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = DrawRountineManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, DrawRountineConfig> drawrountines = new HashMap<>();
			for (Element e : list) {
				DrawRountineConfig drawrountine = new DrawRountineConfig(e);
				drawrountines.put(drawrountine.getId(), drawrountine);
			}			

			this.drawrountines = com.google.common.collect.ImmutableMap.copyOf(drawrountines);

			log.info("load DrawRountineConfig size[{}]", drawrountines.size());

		} catch (Exception e) {
			throw new RuntimeException("load DrawRountineConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
