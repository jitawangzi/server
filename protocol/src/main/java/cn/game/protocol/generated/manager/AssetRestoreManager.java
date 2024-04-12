package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.AssetRestoreConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class AssetRestoreManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(AssetRestoreManager.class);

	private static AssetRestoreManager instance = new AssetRestoreManager();
	private static final String xmlFileName = "AssetRestore";
	
	/** 总数据，按id取值 */
	private Map<Integer, AssetRestoreConfig> assetrestores = new HashMap<>();

	public static AssetRestoreManager instance() {
		return instance;
	}
	private AssetRestoreManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public AssetRestoreConfig get(int id) {
		AssetRestoreConfig config = this.assetrestores.get(id);
		if (config == null) { 
			throw new NullPointerException("【AssetRestore】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public AssetRestoreConfig getNullable(int id) {
		return this.assetrestores.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<AssetRestoreConfig> list() {
		return this.assetrestores.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, AssetRestoreConfig> assetrestores = new HashMap<>();
			for (Element e : list) {
				AssetRestoreConfig assetrestore = new AssetRestoreConfig(e);
				AssetRestoreConfig old = assetrestores.put(assetrestore.ID, assetrestore);
				if (old != null) {
					throw new IllegalArgumentException("[AssetRestoreConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.assetrestores = com.google.common.collect.ImmutableMap.copyOf(assetrestores);

			log.info("load AssetRestoreConfig size[{}]", assetrestores.size());

		} catch (Exception e) {
			throw new RuntimeException("load AssetRestoreConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
