package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.MainEventEffectConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class MainEventEffectManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(MainEventEffectManager.class);

	private static MainEventEffectManager instance = new MainEventEffectManager();
	private static final String xmlFileName = "MainEventEffect";
	
	private Map<Integer, MainEventEffectConfig> maineventeffects = new HashMap<>();

	public static MainEventEffectManager getInstance() {
		return instance;
	}

	private MainEventEffectManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public MainEventEffectConfig getMainEventEffectConfig(int id) {
		MainEventEffectConfig config = this.maineventeffects.get(id);
		if (config == null) { 
			throw new NullPointerException("【MainEventEffect】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public MainEventEffectConfig getMainEventEffectConfigNullable(int id) {
		return this.maineventeffects.get(id);
	}

	public Collection<MainEventEffectConfig> list() {
		return this.maineventeffects.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = MainEventEffectManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, MainEventEffectConfig> map = new HashMap<>();
			for (Element e : list) {
				MainEventEffectConfig maineventeffect = new MainEventEffectConfig(e);
				map.put(maineventeffect.getId(), maineventeffect);
			}
			
			this.maineventeffects = map;

			log.info("load MainEventEffectConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load MainEventEffectConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
