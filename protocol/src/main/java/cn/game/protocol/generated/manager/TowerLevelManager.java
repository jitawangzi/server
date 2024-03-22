package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.TowerLevelConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class TowerLevelManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(TowerLevelManager.class);

	private static TowerLevelManager instance = new TowerLevelManager();
	private static final String xmlFileName = "TowerLevel";
	
	private Map<Integer, TowerLevelConfig> towerlevels = new HashMap<>();

	public static TowerLevelManager getInstance() {
		return instance;
	}

	private TowerLevelManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public TowerLevelConfig getTowerLevelConfig(int id) {
		TowerLevelConfig config = this.towerlevels.get(id);
		if (config == null) { 
			throw new NullPointerException("【TowerLevel】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public TowerLevelConfig getTowerLevelConfigNullable(int id) {
		return this.towerlevels.get(id);
	}

	public Collection<TowerLevelConfig> list() {
		return this.towerlevels.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = TowerLevelManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, TowerLevelConfig> towerlevels = new HashMap<>();
			for (Element e : list) {
				TowerLevelConfig towerlevel = new TowerLevelConfig(e);
				towerlevels.put(towerlevel.getId(), towerlevel);
			}			

			this.towerlevels = com.google.common.collect.ImmutableMap.copyOf(towerlevels);

			log.info("load TowerLevelConfig size[{}]", towerlevels.size());

		} catch (Exception e) {
			throw new RuntimeException("load TowerLevelConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
