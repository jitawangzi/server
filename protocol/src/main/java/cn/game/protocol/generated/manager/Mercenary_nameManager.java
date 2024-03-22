package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.Mercenary_nameConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

public class Mercenary_nameManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(Mercenary_nameManager.class);

	private static Mercenary_nameManager instance = new Mercenary_nameManager();
	public static final String xmlFileName = "Mercenary_name";
	
	private Map<Integer, Mercenary_nameConfig> mercenary_names = new HashMap<>();

	public static Mercenary_nameManager getInstance() {
		return instance;
	}

	private Mercenary_nameManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public Mercenary_nameConfig getMercenary_nameConfig(int id) {
		Mercenary_nameConfig config = this.mercenary_names.get(id);
		if (config == null) { 
			throw new NullPointerException("【Mercenary_name】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public Mercenary_nameConfig getMercenary_nameConfigNullable(int id) {
		return this.mercenary_names.get(id);
	}

	public Collection<Mercenary_nameConfig> list() {
		return this.mercenary_names.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = Mercenary_nameManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, Mercenary_nameConfig> map = new HashMap<>();
			for (Element e : list) {
				Mercenary_nameConfig mercenary_name = new Mercenary_nameConfig(e);
				map.put(mercenary_name.getId(), mercenary_name);
			}
			
			this.mercenary_names = map;

			log.info("load Mercenary_nameConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load Mercenary_nameConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
