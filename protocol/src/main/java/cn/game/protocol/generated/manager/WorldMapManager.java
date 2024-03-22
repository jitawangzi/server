package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.WorldMapConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class WorldMapManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(WorldMapManager.class);

	private static WorldMapManager instance = new WorldMapManager();
	private static final String xmlFileName = "WorldMap";
	
	private Map<Integer, WorldMapConfig> worldmaps = new HashMap<>();

	public static WorldMapManager getInstance() {
		return instance;
	}

	private WorldMapManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public WorldMapConfig getWorldMapConfig(int id) {
		WorldMapConfig config = this.worldmaps.get(id);
		if (config == null) { 
			throw new NullPointerException("【WorldMap】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public WorldMapConfig getWorldMapConfigNullable(int id) {
		return this.worldmaps.get(id);
	}

	public Collection<WorldMapConfig> list() {
		return this.worldmaps.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = WorldMapManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, WorldMapConfig> worldmaps = new HashMap<>();
			for (Element e : list) {
				WorldMapConfig worldmap = new WorldMapConfig(e);
				worldmaps.put(worldmap.getId(), worldmap);
			}			

			this.worldmaps = com.google.common.collect.ImmutableMap.copyOf(worldmaps);

			log.info("load WorldMapConfig size[{}]", worldmaps.size());

		} catch (Exception e) {
			throw new RuntimeException("load WorldMapConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
