package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ChipConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class ChipManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ChipManager.class);

	private static ChipManager instance = new ChipManager();
	private static final String xmlFileName = "Chip";
	
	private Map<Integer, ChipConfig> chips = new HashMap<>();

	public static ChipManager getInstance() {
		return instance;
	}

	private ChipManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public ChipConfig getChipConfig(int id) {
		ChipConfig config = this.chips.get(id);
		if (config == null) { 
			throw new NullPointerException("【Chip】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public ChipConfig getChipConfigNullable(int id) {
		return this.chips.get(id);
	}

	public Collection<ChipConfig> list() {
		return this.chips.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = ChipManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, ChipConfig> chips = new HashMap<>();
			for (Element e : list) {
				ChipConfig chip = new ChipConfig(e);
				chips.put(chip.getId(), chip);
			}			

			this.chips = com.google.common.collect.ImmutableMap.copyOf(chips);

			log.info("load ChipConfig size[{}]", chips.size());

		} catch (Exception e) {
			throw new RuntimeException("load ChipConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
