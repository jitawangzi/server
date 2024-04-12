package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.DragonSkillConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class DragonSkillManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(DragonSkillManager.class);

	private static DragonSkillManager instance = new DragonSkillManager();
	private static final String xmlFileName = "DragonSkill";
	
	/** 总数据，按id取值 */
	private Map<Integer, DragonSkillConfig> dragonskills = new HashMap<>();

	public static DragonSkillManager instance() {
		return instance;
	}
	private DragonSkillManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public DragonSkillConfig get(int id) {
		DragonSkillConfig config = this.dragonskills.get(id);
		if (config == null) { 
			throw new NullPointerException("【DragonSkill】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public DragonSkillConfig getNullable(int id) {
		return this.dragonskills.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<DragonSkillConfig> list() {
		return this.dragonskills.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, DragonSkillConfig> dragonskills = new HashMap<>();
			for (Element e : list) {
				DragonSkillConfig dragonskill = new DragonSkillConfig(e);
				DragonSkillConfig old = dragonskills.put(dragonskill.ID, dragonskill);
				if (old != null) {
					throw new IllegalArgumentException("[DragonSkillConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.dragonskills = com.google.common.collect.ImmutableMap.copyOf(dragonskills);

			log.info("load DragonSkillConfig size[{}]", dragonskills.size());

		} catch (Exception e) {
			throw new RuntimeException("load DragonSkillConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
