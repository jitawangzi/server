package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.PatrolConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class PatrolManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(PatrolManager.class);

	private static PatrolManager instance = new PatrolManager();
	private static final String xmlFileName = "Patrol";
	
	/** 总数据，按id取值 */
	private Map<Integer, PatrolConfig> patrols = new HashMap<>();

	public static PatrolManager instance() {
		return instance;
	}
	private PatrolManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public PatrolConfig get(int id) {
		PatrolConfig config = this.patrols.get(id);
		if (config == null) { 
			throw new NullPointerException("【Patrol】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public PatrolConfig getNullable(int id) {
		return this.patrols.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<PatrolConfig> list() {
		return this.patrols.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, PatrolConfig> patrols = new HashMap<>();
			for (Element e : list) {
				PatrolConfig patrol = new PatrolConfig(e);
				PatrolConfig old = patrols.put(patrol.ID, patrol);
				if (old != null) {
					throw new IllegalArgumentException("[PatrolConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.patrols = com.google.common.collect.ImmutableMap.copyOf(patrols);

			log.info("load PatrolConfig size[{}]", patrols.size());

		} catch (Exception e) {
			throw new RuntimeException("load PatrolConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
