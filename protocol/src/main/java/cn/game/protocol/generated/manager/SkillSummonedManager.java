package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.SkillSummonedConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class SkillSummonedManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(SkillSummonedManager.class);

	private static SkillSummonedManager instance = new SkillSummonedManager();
	private static final String xmlFileName = "SkillSummoned";
	
	private Map<Integer, SkillSummonedConfig> skillsummoneds = new HashMap<>();

	public static SkillSummonedManager getInstance() {
		return instance;
	}

	private SkillSummonedManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public SkillSummonedConfig getSkillSummonedConfig(int id) {
		SkillSummonedConfig config = this.skillsummoneds.get(id);
		if (config == null) { 
			throw new NullPointerException("【SkillSummoned】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public SkillSummonedConfig getSkillSummonedConfigNullable(int id) {
		return this.skillsummoneds.get(id);
	}

	public Collection<SkillSummonedConfig> list() {
		return this.skillsummoneds.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = SkillSummonedManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, SkillSummonedConfig> skillsummoneds = new HashMap<>();
			for (Element e : list) {
				SkillSummonedConfig skillsummoned = new SkillSummonedConfig(e);
				skillsummoneds.put(skillsummoned.getId(), skillsummoned);
			}			

			this.skillsummoneds = com.google.common.collect.ImmutableMap.copyOf(skillsummoneds);

			log.info("load SkillSummonedConfig size[{}]", skillsummoneds.size());

		} catch (Exception e) {
			throw new RuntimeException("load SkillSummonedConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
