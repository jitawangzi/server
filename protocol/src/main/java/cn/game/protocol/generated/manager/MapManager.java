package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.MapConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class MapManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(MapManager.class);

	private static MapManager instance = new MapManager();
	private static final String xmlFileName = "Map";
	
	/** 总数据，按id取值 */
	private Map<Integer, MapConfig> maps = new HashMap<>();

	public static MapManager instance() {
		return instance;
	}
	private MapManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public MapConfig get(int id) {
		MapConfig config = this.maps.get(id);
		if (config == null) { 
			throw new NullPointerException("【Map】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public MapConfig getNullable(int id) {
		return this.maps.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<MapConfig> list() {
		return this.maps.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, MapConfig> maps = new HashMap<>();
			for (Element e : list) {
				MapConfig map = new MapConfig(e);
				MapConfig old = maps.put(map.ID, map);
				if (old != null) {
					throw new IllegalArgumentException("[MapConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.maps = com.google.common.collect.ImmutableMap.copyOf(maps);

			log.info("load MapConfig size[{}]", maps.size());

		} catch (Exception e) {
			throw new RuntimeException("load MapConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
