package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.DrawGroupConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class DrawGroupManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(DrawGroupManager.class);

	private static DrawGroupManager instance = new DrawGroupManager();
	private static final String xmlFileName = "DrawGroup";
	
	private Map<Integer, DrawGroupConfig> drawgroups = new HashMap<>();

	public static DrawGroupManager getInstance() {
		return instance;
	}

	private DrawGroupManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public DrawGroupConfig getDrawGroupConfig(int id) {
		DrawGroupConfig config = this.drawgroups.get(id);
		if (config == null) { 
			throw new NullPointerException("【DrawGroup】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public DrawGroupConfig getDrawGroupConfigNullable(int id) {
		return this.drawgroups.get(id);
	}

	public Collection<DrawGroupConfig> list() {
		return this.drawgroups.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = DrawGroupManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, DrawGroupConfig> drawgroups = new HashMap<>();
			for (Element e : list) {
				DrawGroupConfig drawgroup = new DrawGroupConfig(e);
				drawgroups.put(drawgroup.getId(), drawgroup);
			}			

			this.drawgroups = com.google.common.collect.ImmutableMap.copyOf(drawgroups);

			log.info("load DrawGroupConfig size[{}]", drawgroups.size());

		} catch (Exception e) {
			throw new RuntimeException("load DrawGroupConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
