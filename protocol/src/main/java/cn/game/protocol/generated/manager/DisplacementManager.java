package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.DisplacementConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class DisplacementManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(DisplacementManager.class);

	private static DisplacementManager instance = new DisplacementManager();
	private static final String xmlFileName = "Displacement";
	
	/** 总数据，按id取值 */
	private Map<Integer, DisplacementConfig> displacements = new HashMap<>();

	public static DisplacementManager instance() {
		return instance;
	}
	private DisplacementManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public DisplacementConfig get(int id) {
		DisplacementConfig config = this.displacements.get(id);
		if (config == null) { 
			throw new NullPointerException("【Displacement】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public DisplacementConfig getNullable(int id) {
		return this.displacements.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<DisplacementConfig> list() {
		return this.displacements.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, DisplacementConfig> displacements = new HashMap<>();
			for (Element e : list) {
				DisplacementConfig displacement = new DisplacementConfig(e);
				DisplacementConfig old = displacements.put(displacement.ID, displacement);
				if (old != null) {
					throw new IllegalArgumentException("[DisplacementConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.displacements = com.google.common.collect.ImmutableMap.copyOf(displacements);

			log.info("load DisplacementConfig size[{}]", displacements.size());

		} catch (Exception e) {
			throw new RuntimeException("load DisplacementConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
