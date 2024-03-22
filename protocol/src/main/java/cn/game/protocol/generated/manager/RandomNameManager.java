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

import cn.game.protocol.generated.config.RandomNameConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RandomNameManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RandomNameManager.class);

	private static RandomNameManager instance = new RandomNameManager();
	private static final String xmlFileName = "RandomName";
	
	private List<RandomNameConfig> randomnames = new ArrayList<RandomNameConfig>();

	public static RandomNameManager instance() {
		return instance;
	}
	private RandomNameManager() {
		WatchServiceManager.getInstance().register(this);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public List<RandomNameConfig> list() {
		return randomnames;
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			List<RandomNameConfig> randomnames = new ArrayList<RandomNameConfig>();
			for (Element e : list) {
				RandomNameConfig randomname = new RandomNameConfig(e);
				randomnames.add(randomname);
			}			

			this.randomnames = com.google.common.collect.ImmutableList.copyOf(randomnames);

			log.info("load RandomNameConfig size[{}]", randomnames.size());

		} catch (Exception e) {
			throw new RuntimeException("load RandomNameConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
