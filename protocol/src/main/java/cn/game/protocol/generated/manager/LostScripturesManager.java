package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.LostScripturesConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class LostScripturesManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(LostScripturesManager.class);

	private static LostScripturesManager instance = new LostScripturesManager();
	private static final String xmlFileName = "LostScriptures";
	
	/** 总数据，按id取值 */
	private Map<Integer, LostScripturesConfig> lostscripturess = new HashMap<>();

	public static LostScripturesManager instance() {
		return instance;
	}
	private LostScripturesManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public LostScripturesConfig get(int id) {
		LostScripturesConfig config = this.lostscripturess.get(id);
		if (config == null) { 
			throw new NullPointerException("【LostScriptures】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public LostScripturesConfig getNullable(int id) {
		return this.lostscripturess.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<LostScripturesConfig> list() {
		return this.lostscripturess.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, LostScripturesConfig> lostscripturess = new HashMap<>();
			for (Element e : list) {
				LostScripturesConfig lostscriptures = new LostScripturesConfig(e);
				LostScripturesConfig old = lostscripturess.put(lostscriptures.ID, lostscriptures);
				if (old != null) {
					throw new IllegalArgumentException("[LostScripturesConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.lostscripturess = com.google.common.collect.ImmutableMap.copyOf(lostscripturess);

			log.info("load LostScripturesConfig size[{}]", lostscripturess.size());

		} catch (Exception e) {
			throw new RuntimeException("load LostScripturesConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
