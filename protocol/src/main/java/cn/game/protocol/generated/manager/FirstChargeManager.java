package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.FirstChargeConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class FirstChargeManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(FirstChargeManager.class);

	private static FirstChargeManager instance = new FirstChargeManager();
	private static final String xmlFileName = "FirstCharge";
	
	/** 总数据，按id取值 */
	private Map<Integer, FirstChargeConfig> firstcharges = new HashMap<>();

	public static FirstChargeManager instance() {
		return instance;
	}
	private FirstChargeManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public FirstChargeConfig get(int id) {
		FirstChargeConfig config = this.firstcharges.get(id);
		if (config == null) { 
			throw new NullPointerException("【FirstCharge】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public FirstChargeConfig getNullable(int id) {
		return this.firstcharges.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<FirstChargeConfig> list() {
		return this.firstcharges.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, FirstChargeConfig> firstcharges = new HashMap<>();
			for (Element e : list) {
				FirstChargeConfig firstcharge = new FirstChargeConfig(e);
				FirstChargeConfig old = firstcharges.put(firstcharge.ID, firstcharge);
				if (old != null) {
					throw new IllegalArgumentException("[FirstChargeConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.firstcharges = com.google.common.collect.ImmutableMap.copyOf(firstcharges);

			log.info("load FirstChargeConfig size[{}]", firstcharges.size());

		} catch (Exception e) {
			throw new RuntimeException("load FirstChargeConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
