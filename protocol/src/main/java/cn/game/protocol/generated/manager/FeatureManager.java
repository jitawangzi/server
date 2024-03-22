package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.FeatureConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class FeatureManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(FeatureManager.class);

	private static FeatureManager instance = new FeatureManager();
	private static final String xmlFileName = "Feature";
	
	private Map<Integer, FeatureConfig> features = new HashMap<>();

	public static FeatureManager getInstance() {
		return instance;
	}

	private FeatureManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public FeatureConfig getFeatureConfig(int id) {
		FeatureConfig config = this.features.get(id);
		if (config == null) { 
			throw new NullPointerException("【Feature】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public FeatureConfig getFeatureConfigNullable(int id) {
		return this.features.get(id);
	}

	public Collection<FeatureConfig> list() {
		return this.features.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = FeatureManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, FeatureConfig> features = new HashMap<>();
			for (Element e : list) {
				FeatureConfig feature = new FeatureConfig(e);
				features.put(feature.getId(), feature);
			}			

			this.features = com.google.common.collect.ImmutableMap.copyOf(features);

			log.info("load FeatureConfig size[{}]", features.size());

		} catch (Exception e) {
			log.error("load FeatureConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
