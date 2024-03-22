package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.TrapConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class TrapManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(TrapManager.class);

	private static TrapManager instance = new TrapManager();
	private static final String xmlFileName = "Trap";
	
	private Map<Integer, TrapConfig> traps = new HashMap<>();

	public static TrapManager getInstance() {
		return instance;
	}

	private TrapManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public TrapConfig getTrapConfig(int id) {
		TrapConfig config = this.traps.get(id);
		if (config == null) { 
			throw new NullPointerException("【Trap】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public TrapConfig getTrapConfigNullable(int id) {
		return this.traps.get(id);
	}

	public Collection<TrapConfig> list() {
		return this.traps.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = TrapManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, TrapConfig> traps = new HashMap<>();
			for (Element e : list) {
				TrapConfig trap = new TrapConfig(e);
				traps.put(trap.getId(), trap);
			}			

			this.traps = com.google.common.collect.ImmutableMap.copyOf(traps);

			log.info("load TrapConfig size[{}]", traps.size());

		} catch (Exception e) {
			throw new RuntimeException("load TrapConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
