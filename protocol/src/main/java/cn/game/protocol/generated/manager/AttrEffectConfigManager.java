package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.AttrEffectConfigConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class AttrEffectConfigManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(AttrEffectConfigManager.class);

	private static AttrEffectConfigManager instance = new AttrEffectConfigManager();
	private static final String xmlFileName = "AttrEffectConfig";
	
	private Map<Integer, AttrEffectConfigConfig> attreffectconfigs = new HashMap<>();

	public static AttrEffectConfigManager getInstance() {
		return instance;
	}

	private AttrEffectConfigManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public AttrEffectConfigConfig get(int id) {
		AttrEffectConfigConfig config = this.attreffectconfigs.get(id);
		if (config == null) { 
			throw new NullPointerException("【AttrEffectConfig】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public AttrEffectConfigConfig getNullable(int id) {
		return this.attreffectconfigs.get(id);
	}

	public Collection<AttrEffectConfigConfig> list() {
		return this.attreffectconfigs.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, AttrEffectConfigConfig> attreffectconfigs = new HashMap<>();
			for (Element e : list) {
				AttrEffectConfigConfig attreffectconfig = new AttrEffectConfigConfig(e);
				attreffectconfigs.put(attreffectconfig.getID(), attreffectconfig);
			}			

			this.attreffectconfigs = com.google.common.collect.ImmutableMap.copyOf(attreffectconfigs);

			log.info("load AttrEffectConfigConfig size[{}]", attreffectconfigs.size());

		} catch (Exception e) {
			throw new RuntimeException("load AttrEffectConfigConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
