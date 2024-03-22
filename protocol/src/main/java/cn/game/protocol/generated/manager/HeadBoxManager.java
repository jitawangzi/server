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

import cn.game.protocol.generated.config.HeadBoxConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class HeadBoxManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(HeadBoxManager.class);

	private static HeadBoxManager instance = new HeadBoxManager();
	private static final String xmlFileName = "HeadBox";
	
	private List<HeadBoxConfig> headboxs = new ArrayList<HeadBoxConfig>();

	public static HeadBoxManager instance() {
		return instance;
	}
	private HeadBoxManager() {
		WatchServiceManager.getInstance().register(this);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public List<HeadBoxConfig> list() {
		return headboxs;
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			List<HeadBoxConfig> headboxs = new ArrayList<HeadBoxConfig>();
			for (Element e : list) {
				HeadBoxConfig headbox = new HeadBoxConfig(e);
				headboxs.add(headbox);
			}			

			this.headboxs = com.google.common.collect.ImmutableList.copyOf(headboxs);

			log.info("load HeadBoxConfig size[{}]", headboxs.size());

		} catch (Exception e) {
			throw new RuntimeException("load HeadBoxConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
