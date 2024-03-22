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

import cn.game.protocol.generated.config.MissionDailyConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class MissionDailyManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(MissionDailyManager.class);

	private static MissionDailyManager instance = new MissionDailyManager();
	private static final String xmlFileName = "MissionDaily";
	
	private List<MissionDailyConfig> missiondailys = new ArrayList<MissionDailyConfig>();

	public static MissionDailyManager getInstance() {
		return instance;
	}

	private MissionDailyManager() {
		WatchServiceManager.getInstance().register(this);
	}

	public List<MissionDailyConfig> list() {
		return missiondailys;
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = MissionDailyManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			List<MissionDailyConfig> missiondailys = new ArrayList<MissionDailyConfig>();
			for (Element e : list) {
				MissionDailyConfig missiondaily = new MissionDailyConfig(e);
				missiondailys.add(missiondaily);
			}			

			this.missiondailys = com.google.common.collect.ImmutableList.copyOf(missiondailys);

			log.info("load MissionDailyConfig size[{}]", missiondailys.size());

		} catch (Exception e) {
			throw new RuntimeException("load MissionDailyConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
