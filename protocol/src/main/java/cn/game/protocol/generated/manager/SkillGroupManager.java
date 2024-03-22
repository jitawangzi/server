package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.SkillGroupConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class SkillGroupManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(SkillGroupManager.class);

	private static SkillGroupManager instance = new SkillGroupManager();
	private static final String xmlFileName = "SkillGroup";
	
	/** 总数据，按id取值 */
	private Map<Integer, SkillGroupConfig> skillgroups = new HashMap<>();

	public static SkillGroupManager instance() {
		return instance;
	}
	private SkillGroupManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public SkillGroupConfig get(int id) {
		SkillGroupConfig config = this.skillgroups.get(id);
		if (config == null) { 
			throw new NullPointerException("【SkillGroup】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public SkillGroupConfig getNullable(int id) {
		return this.skillgroups.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<SkillGroupConfig> list() {
		return this.skillgroups.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, SkillGroupConfig> skillgroups = new HashMap<>();
			for (Element e : list) {
				SkillGroupConfig skillgroup = new SkillGroupConfig(e);
				SkillGroupConfig old = skillgroups.put(skillgroup.ID, skillgroup);
				if (old != null) {
					throw new IllegalArgumentException("[SkillGroupConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.skillgroups = com.google.common.collect.ImmutableMap.copyOf(skillgroups);

			log.info("load SkillGroupConfig size[{}]", skillgroups.size());

		} catch (Exception e) {
			throw new RuntimeException("load SkillGroupConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
