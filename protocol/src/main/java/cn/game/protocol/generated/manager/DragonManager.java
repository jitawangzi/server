package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.DragonConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class DragonManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(DragonManager.class);

	private static DragonManager instance = new DragonManager();
	private static final String xmlFileName = "Dragon";
	
	/** 总数据，按id取值 */
	private Map<Integer, DragonConfig> dragons = new HashMap<>();

	public static DragonManager instance() {
		return instance;
	}
	private DragonManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public DragonConfig get(int id) {
		DragonConfig config = this.dragons.get(id);
		if (config == null) { 
			throw new NullPointerException("【Dragon】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public DragonConfig getNullable(int id) {
		return this.dragons.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<DragonConfig> list() {
		return this.dragons.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, DragonConfig> dragons = new HashMap<>();
			for (Element e : list) {
				DragonConfig dragon = new DragonConfig(e);
				DragonConfig old = dragons.put(dragon.ID, dragon);
				if (old != null) {
					throw new IllegalArgumentException("[DragonConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.dragons = com.google.common.collect.ImmutableMap.copyOf(dragons);

			log.info("load DragonConfig size[{}]", dragons.size());

		} catch (Exception e) {
			throw new RuntimeException("load DragonConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
