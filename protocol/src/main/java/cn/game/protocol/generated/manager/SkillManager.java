package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.SkillConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class SkillManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(SkillManager.class);

	private static SkillManager instance = new SkillManager();
	private static final String xmlFileName = "Skill";
	
	/** 总数据，按id取值 */
	private Map<Integer, SkillConfig> skills = new HashMap<>();

	public static SkillManager instance() {
		return instance;
	}
	private SkillManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public SkillConfig get(int id) {
		SkillConfig config = this.skills.get(id);
		if (config == null) { 
			throw new NullPointerException("【Skill】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public SkillConfig getNullable(int id) {
		return this.skills.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<SkillConfig> list() {
		return this.skills.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, SkillConfig> skills = new HashMap<>();
			for (Element e : list) {
				SkillConfig skill = new SkillConfig(e);
				SkillConfig old = skills.put(skill.ID, skill);
				if (old != null) {
					throw new IllegalArgumentException("[SkillConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.skills = com.google.common.collect.ImmutableMap.copyOf(skills);

			log.info("load SkillConfig size[{}]", skills.size());

		} catch (Exception e) {
			throw new RuntimeException("load SkillConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
