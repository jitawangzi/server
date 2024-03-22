package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.SystemRootConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

public class SystemRootManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(SystemRootManager.class);

	private static SystemRootManager instance = new SystemRootManager();
	public static final String xmlFileName = "SystemRoot";
	
	private Map<Integer, SystemRootConfig> systemroots = new HashMap<>();

	public static SystemRootManager getInstance() {
		return instance;
	}

	private SystemRootManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public SystemRootConfig getSystemRootConfig(int id) {
		SystemRootConfig config = this.systemroots.get(id);
		if (config == null) { 
			throw new NullPointerException("【SystemRoot】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public SystemRootConfig getSystemRootConfigNullable(int id) {
		return this.systemroots.get(id);
	}

	public Collection<SystemRootConfig> list() {
		return this.systemroots.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = SystemRootManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, SystemRootConfig> map = new HashMap<>();
			for (Element e : list) {
				SystemRootConfig systemroot = new SystemRootConfig(e);
				map.put(systemroot.getId(), systemroot);
			}
			
			this.systemroots = map;

			log.info("load SystemRootConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load SystemRootConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
