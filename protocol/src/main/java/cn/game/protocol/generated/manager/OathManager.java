package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.OathConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class OathManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(OathManager.class);

	private static OathManager instance = new OathManager();
	private static final String xmlFileName = "Oath";
	
	private Map<Integer, OathConfig> oaths = new HashMap<>();

	public static OathManager getInstance() {
		return instance;
	}

	private OathManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public OathConfig getOathConfig(int id) {
		OathConfig config = this.oaths.get(id);
		if (config == null) { 
			throw new NullPointerException("【Oath】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public OathConfig getOathConfigNullable(int id) {
		return this.oaths.get(id);
	}

	public Collection<OathConfig> list() {
		return this.oaths.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = OathManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, OathConfig> oaths = new HashMap<>();
			for (Element e : list) {
				OathConfig oath = new OathConfig(e);
				oaths.put(oath.getId(), oath);
			}			

			this.oaths = com.google.common.collect.ImmutableMap.copyOf(oaths);

			log.info("load OathConfig size[{}]", oaths.size());

		} catch (Exception e) {
			throw new RuntimeException("load OathConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
