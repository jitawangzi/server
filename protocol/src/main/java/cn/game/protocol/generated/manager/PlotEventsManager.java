package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.PlotEventsConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

public class PlotEventsManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(PlotEventsManager.class);

	private static PlotEventsManager instance = new PlotEventsManager();
	public static final String xmlFileName = "PlotEvents";
	
	private Map<Integer, PlotEventsConfig> ploteventss = new HashMap<>();

	public static PlotEventsManager getInstance() {
		return instance;
	}

	private PlotEventsManager() {
		WatchServiceManager.getInstance().register(this);
	}
	public PlotEventsConfig getPlotEventsConfig(int id) {
		return this.ploteventss.get(id);
	}

	public Collection<PlotEventsConfig> list() {
		return this.ploteventss.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = PlotEventsManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, PlotEventsConfig> map = new HashMap<>();
			for (Element e : list) {
				PlotEventsConfig plotevents = new PlotEventsConfig(e);
				map.put(plotevents.getId(), plotevents);
			}
			
			this.ploteventss = map;

			log.info("load PlotEventsConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load PlotEventsConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
