package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.EffectConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class EffectManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(EffectManager.class);

	private static EffectManager instance = new EffectManager();
	private static final String xmlFileName = "Effect";
	
	private Map<Integer, EffectConfig> effects = new HashMap<>();

	public static EffectManager getInstance() {
		return instance;
	}

	private EffectManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public EffectConfig get(int id) {
		EffectConfig config = this.effects.get(id);
		if (config == null) { 
			throw new NullPointerException("【Effect】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public EffectConfig getNullable(int id) {
		return this.effects.get(id);
	}

	public Collection<EffectConfig> list() {
		return this.effects.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, EffectConfig> effects = new HashMap<>();
			for (Element e : list) {
				EffectConfig effect = new EffectConfig(e);
				effects.put(effect.getID(), effect);
			}			

			this.effects = com.google.common.collect.ImmutableMap.copyOf(effects);

			log.info("load EffectConfig size[{}]", effects.size());

		} catch (Exception e) {
			throw new RuntimeException("load EffectConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
