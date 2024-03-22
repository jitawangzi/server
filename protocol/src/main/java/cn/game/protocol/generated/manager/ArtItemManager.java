package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ArtItemConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class ArtItemManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ArtItemManager.class);

	private static ArtItemManager instance = new ArtItemManager();
	private static final String xmlFileName = "ArtItem";
	
	private Map<Integer, ArtItemConfig> artitems = new HashMap<>();

	public static ArtItemManager getInstance() {
		return instance;
	}

	private ArtItemManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public ArtItemConfig getArtItemConfig(int id) {
		ArtItemConfig config = this.artitems.get(id);
		if (config == null) { 
			throw new NullPointerException("【ArtItem】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public ArtItemConfig getArtItemConfigNullable(int id) {
		return this.artitems.get(id);
	}

	public Collection<ArtItemConfig> list() {
		return this.artitems.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = ArtItemManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, ArtItemConfig> map = new HashMap<>();
			for (Element e : list) {
				ArtItemConfig artitem = new ArtItemConfig(e);
				map.put(artitem.getId(), artitem);
			}
			
			this.artitems = map;

			log.info("load ArtItemConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load ArtItemConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
