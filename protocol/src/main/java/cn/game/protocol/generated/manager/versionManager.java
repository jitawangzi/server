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

import cn.game.protocol.generated.config.versionConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class versionManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(versionManager.class);

	private static versionManager instance = new versionManager();
	private static final String xmlFileName = "version";
	
	private List<versionConfig> versions = new ArrayList<versionConfig>();

	public static versionManager getInstance() {
		return instance;
	}

	private versionManager() {
		WatchServiceManager.getInstance().register(this);
	}

	public List<versionConfig> list() {
		return versions;
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = versionManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			List<versionConfig> versions = new ArrayList<versionConfig>();
			for (Element e : list) {
				versionConfig version = new versionConfig(e);
				versions.add(version);
			}			

			this.versions = com.google.common.collect.ImmutableList.copyOf(versions);

			log.info("load versionConfig size[{}]", versions.size());

		} catch (Exception e) {
			throw new RuntimeException("load versionConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
