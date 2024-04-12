package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.CoordinateGroupConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class CoordinateGroupManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(CoordinateGroupManager.class);

	private static CoordinateGroupManager instance = new CoordinateGroupManager();
	private static final String xmlFileName = "CoordinateGroup";
	
	/** 总数据，按id取值 */
	private Map<Integer, CoordinateGroupConfig> coordinategroups = new HashMap<>();

	public static CoordinateGroupManager instance() {
		return instance;
	}
	private CoordinateGroupManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public CoordinateGroupConfig get(int id) {
		CoordinateGroupConfig config = this.coordinategroups.get(id);
		if (config == null) { 
			throw new NullPointerException("【CoordinateGroup】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public CoordinateGroupConfig getNullable(int id) {
		return this.coordinategroups.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<CoordinateGroupConfig> list() {
		return this.coordinategroups.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, CoordinateGroupConfig> coordinategroups = new HashMap<>();
			for (Element e : list) {
				CoordinateGroupConfig coordinategroup = new CoordinateGroupConfig(e);
				CoordinateGroupConfig old = coordinategroups.put(coordinategroup.ID, coordinategroup);
				if (old != null) {
					throw new IllegalArgumentException("[CoordinateGroupConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.coordinategroups = com.google.common.collect.ImmutableMap.copyOf(coordinategroups);

			log.info("load CoordinateGroupConfig size[{}]", coordinategroups.size());

		} catch (Exception e) {
			throw new RuntimeException("load CoordinateGroupConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
