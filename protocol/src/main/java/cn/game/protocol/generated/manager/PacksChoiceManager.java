package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.PacksChoiceConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class PacksChoiceManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(PacksChoiceManager.class);

	private static PacksChoiceManager instance = new PacksChoiceManager();
	private static final String xmlFileName = "PacksChoice";
	
	/** 总数据，按id取值 */
	private Map<Integer, PacksChoiceConfig> packschoices = new HashMap<>();

	public static PacksChoiceManager instance() {
		return instance;
	}
	private PacksChoiceManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public PacksChoiceConfig get(int id) {
		PacksChoiceConfig config = this.packschoices.get(id);
		if (config == null) { 
			throw new NullPointerException("【PacksChoice】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public PacksChoiceConfig getNullable(int id) {
		return this.packschoices.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<PacksChoiceConfig> list() {
		return this.packschoices.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, PacksChoiceConfig> packschoices = new HashMap<>();
			for (Element e : list) {
				PacksChoiceConfig packschoice = new PacksChoiceConfig(e);
				PacksChoiceConfig old = packschoices.put(packschoice.ID, packschoice);
				if (old != null) {
					throw new IllegalArgumentException("[PacksChoiceConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.packschoices = com.google.common.collect.ImmutableMap.copyOf(packschoices);

			log.info("load PacksChoiceConfig size[{}]", packschoices.size());

		} catch (Exception e) {
			throw new RuntimeException("load PacksChoiceConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
