package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.AttrEffectCoefficientConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class AttrEffectCoefficientManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(AttrEffectCoefficientManager.class);

	private static AttrEffectCoefficientManager instance = new AttrEffectCoefficientManager();
	private static final String xmlFileName = "AttrEffectCoefficient";
	
	/** 总数据，按id取值 */
	private Map<Integer, AttrEffectCoefficientConfig> attreffectcoefficients = new HashMap<>();

	public static AttrEffectCoefficientManager instance() {
		return instance;
	}
	private AttrEffectCoefficientManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public AttrEffectCoefficientConfig get(int id) {
		AttrEffectCoefficientConfig config = this.attreffectcoefficients.get(id);
		if (config == null) { 
			throw new NullPointerException("【AttrEffectCoefficient】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public AttrEffectCoefficientConfig getNullable(int id) {
		return this.attreffectcoefficients.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<AttrEffectCoefficientConfig> list() {
		return this.attreffectcoefficients.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, AttrEffectCoefficientConfig> attreffectcoefficients = new HashMap<>();
			for (Element e : list) {
				AttrEffectCoefficientConfig attreffectcoefficient = new AttrEffectCoefficientConfig(e);
				AttrEffectCoefficientConfig old = attreffectcoefficients.put(attreffectcoefficient.ID, attreffectcoefficient);
				if (old != null) {
					throw new IllegalArgumentException("[AttrEffectCoefficientConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.attreffectcoefficients = com.google.common.collect.ImmutableMap.copyOf(attreffectcoefficients);

			log.info("load AttrEffectCoefficientConfig size[{}]", attreffectcoefficients.size());

		} catch (Exception e) {
			throw new RuntimeException("load AttrEffectCoefficientConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
