package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.CoreSuitEffectConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class CoreSuitEffectManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(CoreSuitEffectManager.class);

	private static CoreSuitEffectManager instance = new CoreSuitEffectManager();
	private static final String xmlFileName = "CoreSuitEffect";
	
	private Map<Integer, CoreSuitEffectConfig> coresuiteffects = new HashMap<>();

	public static CoreSuitEffectManager getInstance() {
		return instance;
	}

	private CoreSuitEffectManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public CoreSuitEffectConfig getCoreSuitEffectConfig(int id) {
		CoreSuitEffectConfig config = this.coresuiteffects.get(id);
		if (config == null) { 
			throw new NullPointerException("【CoreSuitEffect】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public CoreSuitEffectConfig getCoreSuitEffectConfigNullable(int id) {
		return this.coresuiteffects.get(id);
	}

	public Collection<CoreSuitEffectConfig> list() {
		return this.coresuiteffects.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = CoreSuitEffectManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, CoreSuitEffectConfig> coresuiteffects = new HashMap<>();
			for (Element e : list) {
				CoreSuitEffectConfig coresuiteffect = new CoreSuitEffectConfig(e);
				coresuiteffects.put(coresuiteffect.getId(), coresuiteffect);
			}			

			this.coresuiteffects = com.google.common.collect.ImmutableMap.copyOf(coresuiteffects);

			log.info("load CoreSuitEffectConfig size[{}]", coresuiteffects.size());

		} catch (Exception e) {
			throw new RuntimeException("load CoreSuitEffectConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
