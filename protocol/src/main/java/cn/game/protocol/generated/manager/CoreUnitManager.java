package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.CoreUnitConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class CoreUnitManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(CoreUnitManager.class);

	private static CoreUnitManager instance = new CoreUnitManager();
	private static final String xmlFileName = "CoreUnit";
	
	private Map<Integer, CoreUnitConfig> coreunits = new HashMap<>();

	public static CoreUnitManager getInstance() {
		return instance;
	}

	private CoreUnitManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public CoreUnitConfig getCoreUnitConfig(int id) {
		CoreUnitConfig config = this.coreunits.get(id);
		if (config == null) { 
			throw new NullPointerException("【CoreUnit】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public CoreUnitConfig getCoreUnitConfigNullable(int id) {
		return this.coreunits.get(id);
	}

	public Collection<CoreUnitConfig> list() {
		return this.coreunits.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = CoreUnitManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, CoreUnitConfig> coreunits = new HashMap<>();
			for (Element e : list) {
				CoreUnitConfig coreunit = new CoreUnitConfig(e);
				coreunits.put(coreunit.getId(), coreunit);
			}			

			this.coreunits = com.google.common.collect.ImmutableMap.copyOf(coreunits);

			log.info("load CoreUnitConfig size[{}]", coreunits.size());

		} catch (Exception e) {
			throw new RuntimeException("load CoreUnitConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
