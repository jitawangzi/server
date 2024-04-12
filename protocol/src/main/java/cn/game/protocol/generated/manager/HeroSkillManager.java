package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.HeroSkillConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class HeroSkillManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(HeroSkillManager.class);

	private static HeroSkillManager instance = new HeroSkillManager();
	private static final String xmlFileName = "HeroSkill";
	
	/** 总数据，按id取值 */
	private Map<Integer, HeroSkillConfig> heroskills = new HashMap<>();

	public static HeroSkillManager instance() {
		return instance;
	}
	private HeroSkillManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public HeroSkillConfig get(int id) {
		HeroSkillConfig config = this.heroskills.get(id);
		if (config == null) { 
			throw new NullPointerException("【HeroSkill】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public HeroSkillConfig getNullable(int id) {
		return this.heroskills.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<HeroSkillConfig> list() {
		return this.heroskills.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, HeroSkillConfig> heroskills = new HashMap<>();
			for (Element e : list) {
				HeroSkillConfig heroskill = new HeroSkillConfig(e);
				HeroSkillConfig old = heroskills.put(heroskill.ID, heroskill);
				if (old != null) {
					throw new IllegalArgumentException("[HeroSkillConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.heroskills = com.google.common.collect.ImmutableMap.copyOf(heroskills);

			log.info("load HeroSkillConfig size[{}]", heroskills.size());

		} catch (Exception e) {
			throw new RuntimeException("load HeroSkillConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
