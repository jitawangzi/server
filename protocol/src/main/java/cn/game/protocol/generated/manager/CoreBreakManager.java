package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.CoreBreakConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class CoreBreakManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(CoreBreakManager.class);

	private static CoreBreakManager instance = new CoreBreakManager();
	private static final String xmlFileName = "CoreBreak";
	
	private Map<Integer, CoreBreakConfig> corebreaks = new HashMap<>();

	public static CoreBreakManager getInstance() {
		return instance;
	}

	private CoreBreakManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public CoreBreakConfig getCoreBreakConfig(int id) {
		CoreBreakConfig config = this.corebreaks.get(id);
		if (config == null) { 
			throw new NullPointerException("【CoreBreak】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public CoreBreakConfig getCoreBreakConfigNullable(int id) {
		return this.corebreaks.get(id);
	}

	public Collection<CoreBreakConfig> list() {
		return this.corebreaks.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = CoreBreakManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, CoreBreakConfig> corebreaks = new HashMap<>();
			for (Element e : list) {
				CoreBreakConfig corebreak = new CoreBreakConfig(e);
				corebreaks.put(corebreak.getId(), corebreak);
			}			

			this.corebreaks = com.google.common.collect.ImmutableMap.copyOf(corebreaks);

			log.info("load CoreBreakConfig size[{}]", corebreaks.size());

		} catch (Exception e) {
			throw new RuntimeException("load CoreBreakConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
