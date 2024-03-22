package cn.game.protocol.generated.manager;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.StoreDailyConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

public class StoreDailyManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(StoreDailyManager.class);

	private static StoreDailyManager instance = new StoreDailyManager();
	public static final String xmlFileName = "StoreDaily";
	
	private Map<Integer, StoreDailyConfig> storedailys = new HashMap<>();

	public static StoreDailyManager getInstance() {
		return instance;
	}

	private StoreDailyManager() {
		WatchServiceManager.getInstance().register(this);
	}
	public StoreDailyConfig getStoreDailyConfig(int id) {
		return this.storedailys.get(id);
	}

	public Collection<StoreDailyConfig> list() {
		return storedailys.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = StoreDailyManager.class.getClassLoader();
			}
			URL url = classLoader.getResource("xml/" + xmlFileName + ".xml");
			Document document = XmlUtils.load(url.getPath());
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, StoreDailyConfig> map = new HashMap<>();
			for (Element e : list) {
				StoreDailyConfig storedaily = new StoreDailyConfig(e);
				map.put(storedaily.getId(), storedaily);
			}
			
			this.storedailys = map;

			log.info("load StoreDailyConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load StoreDailyConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
