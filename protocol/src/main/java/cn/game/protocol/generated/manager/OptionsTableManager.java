package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.OptionsTableConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

public class OptionsTableManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(OptionsTableManager.class);

	private static OptionsTableManager instance = new OptionsTableManager();
	public static final String xmlFileName = "OptionsTable";
	
	private Map<Integer, OptionsTableConfig> optionstables = new HashMap<>();

	public static OptionsTableManager getInstance() {
		return instance;
	}

	private OptionsTableManager() {
		WatchServiceManager.getInstance().register(this);
	}
	public OptionsTableConfig getOptionsTableConfig(int id) {
		return this.optionstables.get(id);
	}

	public Collection<OptionsTableConfig> list() {
		return this.optionstables.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = OptionsTableManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, OptionsTableConfig> map = new HashMap<>();
			for (Element e : list) {
				OptionsTableConfig optionstable = new OptionsTableConfig(e);
				map.put(optionstable.getId(), optionstable);
			}
			
			this.optionstables = map;

			log.info("load OptionsTableConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load OptionsTableConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
