package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.EventRankIntervalConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class EventRankIntervalManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(EventRankIntervalManager.class);

	private static EventRankIntervalManager instance = new EventRankIntervalManager();
	private static final String xmlFileName = "EventRankInterval";
	
	private List<EventRankIntervalConfig> eventrankintervals = new ArrayList<EventRankIntervalConfig>();

	public static EventRankIntervalManager getInstance() {
		return instance;
	}

	private EventRankIntervalManager() {
		WatchServiceManager.getInstance().register(this);
	}

	public List<EventRankIntervalConfig> list() {
		return eventrankintervals;
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = EventRankIntervalManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			List<EventRankIntervalConfig> eventrankintervals = new ArrayList<EventRankIntervalConfig>();
			for (Element e : list) {
				EventRankIntervalConfig eventrankinterval = new EventRankIntervalConfig(e);
				eventrankintervals.add(eventrankinterval);
			}			

			this.eventrankintervals = com.google.common.collect.ImmutableList.copyOf(eventrankintervals);

			log.info("load EventRankIntervalConfig size[{}]", eventrankintervals.size());

		} catch (Exception e) {
			throw new RuntimeException("load EventRankIntervalConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
