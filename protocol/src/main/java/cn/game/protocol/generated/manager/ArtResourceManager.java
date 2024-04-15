package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ArtResourceConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class ArtResourceManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ArtResourceManager.class);

	private static ArtResourceManager instance = new ArtResourceManager();
	private static final String xmlFileName = "ArtResource";
	
	/** 总数据，按id取值 */
	private Map<Integer, ArtResourceConfig> artresources = new HashMap<>();

	public static ArtResourceManager instance() {
		return instance;
	}
	private ArtResourceManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public ArtResourceConfig get(int id) {
		ArtResourceConfig config = this.artresources.get(id);
		if (config == null) { 
			throw new NullPointerException("【ArtResource】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public ArtResourceConfig getNullable(int id) {
		return this.artresources.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<ArtResourceConfig> list() {
		return this.artresources.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, ArtResourceConfig> artresources = new HashMap<>();
			for (Element e : list) {
				ArtResourceConfig artresource = new ArtResourceConfig(e);
				ArtResourceConfig old = artresources.put(artresource.ID, artresource);
				if (old != null) {
					throw new IllegalArgumentException("[ArtResourceConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.artresources = com.google.common.collect.ImmutableMap.copyOf(artresources);

			log.info("load ArtResourceConfig size[{}]", artresources.size());

		} catch (Exception e) {
			throw new RuntimeException("load ArtResourceConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
