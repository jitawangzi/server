package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.MonsterAppearConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class MonsterAppearManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(MonsterAppearManager.class);

	private static MonsterAppearManager instance = new MonsterAppearManager();
	private static final String xmlFileName = "MonsterAppear";
	
	/** 总数据，按id取值 */
	private Map<Integer, MonsterAppearConfig> monsterappears = new HashMap<>();

	public static MonsterAppearManager instance() {
		return instance;
	}
	private MonsterAppearManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public MonsterAppearConfig get(int id) {
		MonsterAppearConfig config = this.monsterappears.get(id);
		if (config == null) { 
			throw new NullPointerException("【MonsterAppear】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public MonsterAppearConfig getNullable(int id) {
		return this.monsterappears.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<MonsterAppearConfig> list() {
		return this.monsterappears.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, MonsterAppearConfig> monsterappears = new HashMap<>();
			for (Element e : list) {
				MonsterAppearConfig monsterappear = new MonsterAppearConfig(e);
				MonsterAppearConfig old = monsterappears.put(monsterappear.ID, monsterappear);
				if (old != null) {
					throw new IllegalArgumentException("[MonsterAppearConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.monsterappears = com.google.common.collect.ImmutableMap.copyOf(monsterappears);

			log.info("load MonsterAppearConfig size[{}]", monsterappears.size());

		} catch (Exception e) {
			throw new RuntimeException("load MonsterAppearConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
