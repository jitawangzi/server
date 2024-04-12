package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.HeroSkillGroupConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class HeroSkillGroupManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(HeroSkillGroupManager.class);

	private static HeroSkillGroupManager instance = new HeroSkillGroupManager();
	private static final String xmlFileName = "HeroSkillGroup";
	
	/** 总数据，按id取值 */
	private Map<Integer, HeroSkillGroupConfig> heroskillgroups = new HashMap<>();

	public static HeroSkillGroupManager instance() {
		return instance;
	}
	private HeroSkillGroupManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public HeroSkillGroupConfig get(int id) {
		HeroSkillGroupConfig config = this.heroskillgroups.get(id);
		if (config == null) { 
			throw new NullPointerException("【HeroSkillGroup】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public HeroSkillGroupConfig getNullable(int id) {
		return this.heroskillgroups.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<HeroSkillGroupConfig> list() {
		return this.heroskillgroups.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, HeroSkillGroupConfig> heroskillgroups = new HashMap<>();
			for (Element e : list) {
				HeroSkillGroupConfig heroskillgroup = new HeroSkillGroupConfig(e);
				HeroSkillGroupConfig old = heroskillgroups.put(heroskillgroup.ID, heroskillgroup);
				if (old != null) {
					throw new IllegalArgumentException("[HeroSkillGroupConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.heroskillgroups = com.google.common.collect.ImmutableMap.copyOf(heroskillgroups);

			log.info("load HeroSkillGroupConfig size[{}]", heroskillgroups.size());

		} catch (Exception e) {
			throw new RuntimeException("load HeroSkillGroupConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
