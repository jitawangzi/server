package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.MonsterGroupConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class MonsterGroupManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(MonsterGroupManager.class);

	private static MonsterGroupManager instance = new MonsterGroupManager();
	private static final String xmlFileName = "MonsterGroup";
	
	/** 总数据，按id取值 */
	private Map<Integer, MonsterGroupConfig> monstergroups = new HashMap<>();

	public static MonsterGroupManager instance() {
		return instance;
	}
	private MonsterGroupManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public MonsterGroupConfig get(int id) {
		MonsterGroupConfig config = this.monstergroups.get(id);
		if (config == null) { 
			throw new NullPointerException("【MonsterGroup】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public MonsterGroupConfig getNullable(int id) {
		return this.monstergroups.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<MonsterGroupConfig> list() {
		return this.monstergroups.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, MonsterGroupConfig> monstergroups = new HashMap<>();
			for (Element e : list) {
				MonsterGroupConfig monstergroup = new MonsterGroupConfig(e);
				MonsterGroupConfig old = monstergroups.put(monstergroup.ID, monstergroup);
				if (old != null) {
					throw new IllegalArgumentException("[MonsterGroupConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.monstergroups = com.google.common.collect.ImmutableMap.copyOf(monstergroups);

			log.info("load MonsterGroupConfig size[{}]", monstergroups.size());

		} catch (Exception e) {
			throw new RuntimeException("load MonsterGroupConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
