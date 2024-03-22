package cn.game.protocol.generated.manager;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.FriendlyItemConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

public class FriendlyItemManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(FriendlyItemManager.class);

	private static FriendlyItemManager instance = new FriendlyItemManager();
	public static final String xmlFileName = "FriendlyItem";
	private Map<Integer, FriendlyItemConfig> friendlyitems = new HashMap<>();

	public static FriendlyItemManager getInstance() {
		return instance;
	}

	private FriendlyItemManager() {
		WatchServiceManager.getInstance().register(this);
	};

	public FriendlyItemConfig getFriendlyItemConfig(int id) {
		return this.friendlyitems.get(id);
	}
	public Collection<FriendlyItemConfig> list() {
		return friendlyitems.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = FriendlyItemManager.class.getClassLoader();
			}
			URL url = classLoader.getResource("xml/" + xmlFileName + ".xml");
			Document document = XmlUtils.load(url.getPath());
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			Map<Integer, FriendlyItemConfig> map = new HashMap<>();

			for (Element e : list) {
				FriendlyItemConfig friendlyitem = new FriendlyItemConfig(e);
   							map.put(friendlyitem.getId(), friendlyitem);
			}
			this.friendlyitems = map;
			log.info("load FriendlyItemConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load FriendlyItemConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
