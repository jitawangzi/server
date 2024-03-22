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

import cn.game.protocol.generated.config.HeadPortraitConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class HeadPortraitManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(HeadPortraitManager.class);

	private static HeadPortraitManager instance = new HeadPortraitManager();
	private static final String xmlFileName = "HeadPortrait";
	
	private List<HeadPortraitConfig> headportraits = new ArrayList<HeadPortraitConfig>();

	public static HeadPortraitManager instance() {
		return instance;
	}
	private HeadPortraitManager() {
		WatchServiceManager.getInstance().register(this);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public List<HeadPortraitConfig> list() {
		return headportraits;
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			List<HeadPortraitConfig> headportraits = new ArrayList<HeadPortraitConfig>();
			for (Element e : list) {
				HeadPortraitConfig headportrait = new HeadPortraitConfig(e);
				headportraits.add(headportrait);
			}			

			this.headportraits = com.google.common.collect.ImmutableList.copyOf(headportraits);

			log.info("load HeadPortraitConfig size[{}]", headportraits.size());

		} catch (Exception e) {
			throw new RuntimeException("load HeadPortraitConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
