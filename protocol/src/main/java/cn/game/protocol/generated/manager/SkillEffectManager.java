package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.SkillEffectConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

public class SkillEffectManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(SkillEffectManager.class);

	private static SkillEffectManager instance = new SkillEffectManager();
	public static final String xmlFileName = "SkillEffect";
	
	private Map<Integer, SkillEffectConfig> skilleffects = new HashMap<>();

	public static SkillEffectManager getInstance() {
		return instance;
	}

	private SkillEffectManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public SkillEffectConfig getSkillEffectConfig(int id) {
		SkillEffectConfig config = this.skilleffects.get(id);
		if (config == null) { 
			throw new NullPointerException("【SkillEffect】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public SkillEffectConfig getSkillEffectConfigNullable(int id) {
		return this.skilleffects.get(id);
	}

	public Collection<SkillEffectConfig> list() {
		return this.skilleffects.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = SkillEffectManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, SkillEffectConfig> map = new HashMap<>();
			for (Element e : list) {
				SkillEffectConfig skilleffect = new SkillEffectConfig(e);
				map.put(skilleffect.getId(), skilleffect);
			}
			
			this.skilleffects = map;

			log.info("load SkillEffectConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load SkillEffectConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
