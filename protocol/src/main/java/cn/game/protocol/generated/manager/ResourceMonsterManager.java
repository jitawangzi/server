package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ResourceMonsterConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class ResourceMonsterManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ResourceMonsterManager.class);

	private static ResourceMonsterManager instance = new ResourceMonsterManager();
	private static final String xmlFileName = "ResourceMonster";
	
	/** 总数据，按id取值 */
	private Map<Integer, ResourceMonsterConfig> resourcemonsters = new HashMap<>();

	public static ResourceMonsterManager instance() {
		return instance;
	}
	private ResourceMonsterManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public ResourceMonsterConfig get(int id) {
		ResourceMonsterConfig config = this.resourcemonsters.get(id);
		if (config == null) { 
			throw new NullPointerException("【ResourceMonster】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public ResourceMonsterConfig getNullable(int id) {
		return this.resourcemonsters.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<ResourceMonsterConfig> list() {
		return this.resourcemonsters.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, ResourceMonsterConfig> resourcemonsters = new HashMap<>();
			for (Element e : list) {
				ResourceMonsterConfig resourcemonster = new ResourceMonsterConfig(e);
				ResourceMonsterConfig old = resourcemonsters.put(resourcemonster.ID, resourcemonster);
				if (old != null) {
					throw new IllegalArgumentException("[ResourceMonsterConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.resourcemonsters = com.google.common.collect.ImmutableMap.copyOf(resourcemonsters);

			log.info("load ResourceMonsterConfig size[{}]", resourcemonsters.size());

		} catch (Exception e) {
			throw new RuntimeException("load ResourceMonsterConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
