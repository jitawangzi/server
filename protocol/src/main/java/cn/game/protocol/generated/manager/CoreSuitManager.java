package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.CoreSuitConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class CoreSuitManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(CoreSuitManager.class);

	private static CoreSuitManager instance = new CoreSuitManager();
	private static final String xmlFileName = "CoreSuit";
	
	private Map<Integer, CoreSuitConfig> coresuits = new HashMap<>();

	public static CoreSuitManager getInstance() {
		return instance;
	}

	private CoreSuitManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public CoreSuitConfig getCoreSuitConfig(int id) {
		CoreSuitConfig config = this.coresuits.get(id);
		if (config == null) { 
			throw new NullPointerException("【CoreSuit】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public CoreSuitConfig getCoreSuitConfigNullable(int id) {
		return this.coresuits.get(id);
	}

	public Collection<CoreSuitConfig> list() {
		return this.coresuits.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = CoreSuitManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, CoreSuitConfig> coresuits = new HashMap<>();
			for (Element e : list) {
				CoreSuitConfig coresuit = new CoreSuitConfig(e);
				coresuits.put(coresuit.getId(), coresuit);
			}			

			this.coresuits = com.google.common.collect.ImmutableMap.copyOf(coresuits);

			log.info("load CoreSuitConfig size[{}]", coresuits.size());

		} catch (Exception e) {
			throw new RuntimeException("load CoreSuitConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
