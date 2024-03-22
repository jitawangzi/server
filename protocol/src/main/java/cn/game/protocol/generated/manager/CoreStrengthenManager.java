package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.CoreStrengthenConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class CoreStrengthenManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(CoreStrengthenManager.class);

	private static CoreStrengthenManager instance = new CoreStrengthenManager();
	private static final String xmlFileName = "CoreStrengthen";
	
	private Map<Integer, CoreStrengthenConfig> corestrengthens = new HashMap<>();

	public static CoreStrengthenManager getInstance() {
		return instance;
	}

	private CoreStrengthenManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public CoreStrengthenConfig getCoreStrengthenConfig(int id) {
		CoreStrengthenConfig config = this.corestrengthens.get(id);
		if (config == null) { 
			throw new NullPointerException("【CoreStrengthen】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public CoreStrengthenConfig getCoreStrengthenConfigNullable(int id) {
		return this.corestrengthens.get(id);
	}

	public Collection<CoreStrengthenConfig> list() {
		return this.corestrengthens.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = CoreStrengthenManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, CoreStrengthenConfig> corestrengthens = new HashMap<>();
			for (Element e : list) {
				CoreStrengthenConfig corestrengthen = new CoreStrengthenConfig(e);
				corestrengthens.put(corestrengthen.getId(), corestrengthen);
			}			

			this.corestrengthens = com.google.common.collect.ImmutableMap.copyOf(corestrengthens);

			log.info("load CoreStrengthenConfig size[{}]", corestrengthens.size());

		} catch (Exception e) {
			throw new RuntimeException("load CoreStrengthenConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
