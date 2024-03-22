package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ChipStrengthenConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class ChipStrengthenManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ChipStrengthenManager.class);

	private static ChipStrengthenManager instance = new ChipStrengthenManager();
	private static final String xmlFileName = "ChipStrengthen";
	
	private Map<Integer, ChipStrengthenConfig> chipstrengthens = new HashMap<>();

	public static ChipStrengthenManager getInstance() {
		return instance;
	}

	private ChipStrengthenManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public ChipStrengthenConfig getChipStrengthenConfig(int id) {
		ChipStrengthenConfig config = this.chipstrengthens.get(id);
		if (config == null) { 
			throw new NullPointerException("【ChipStrengthen】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public ChipStrengthenConfig getChipStrengthenConfigNullable(int id) {
		return this.chipstrengthens.get(id);
	}

	public Collection<ChipStrengthenConfig> list() {
		return this.chipstrengthens.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = ChipStrengthenManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, ChipStrengthenConfig> chipstrengthens = new HashMap<>();
			for (Element e : list) {
				ChipStrengthenConfig chipstrengthen = new ChipStrengthenConfig(e);
				chipstrengthens.put(chipstrengthen.getId(), chipstrengthen);
			}			

			this.chipstrengthens = com.google.common.collect.ImmutableMap.copyOf(chipstrengthens);

			log.info("load ChipStrengthenConfig size[{}]", chipstrengthens.size());

		} catch (Exception e) {
			throw new RuntimeException("load ChipStrengthenConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
