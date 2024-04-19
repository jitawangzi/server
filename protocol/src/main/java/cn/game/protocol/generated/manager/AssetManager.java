package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.AssetConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class AssetManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(AssetManager.class);

	private static AssetManager instance = new AssetManager();
	private static final String xmlFileName = "Asset";
	
	private Map<Integer, AssetConfig> assets = new HashMap<>();

	public static AssetManager instance() {
		return instance;
	}

	private AssetManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public AssetConfig get(int id) {
		AssetConfig config = this.assets.get(id);
		if (config == null) { 
			throw new NullPointerException("【Asset】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public AssetConfig getNullable(int id) {
		return this.assets.get(id);
	}

	public Collection<AssetConfig> list() {
		return this.assets.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, AssetConfig> assets = new HashMap<>();
			for (Element e : list) {
				AssetConfig asset = new AssetConfig(e);
				assets.put(asset.ID, asset);
			}			

			this.assets = com.google.common.collect.ImmutableMap.copyOf(assets);

			log.info("load AssetConfig size[{}]", assets.size());

		} catch (Exception e) {
			throw new RuntimeException("load AssetConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
