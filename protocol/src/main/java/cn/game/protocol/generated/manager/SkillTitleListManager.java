package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.SkillTitleListConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class SkillTitleListManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(SkillTitleListManager.class);

	private static SkillTitleListManager instance = new SkillTitleListManager();
	private static final String xmlFileName = "SkillTitleList";
	
	private Map<Integer, SkillTitleListConfig> skilltitlelists = new HashMap<>();

	public static SkillTitleListManager getInstance() {
		return instance;
	}

	private SkillTitleListManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public SkillTitleListConfig getSkillTitleListConfig(int id) {
		SkillTitleListConfig config = this.skilltitlelists.get(id);
		if (config == null) { 
			throw new NullPointerException("【SkillTitleList】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public SkillTitleListConfig getSkillTitleListConfigNullable(int id) {
		return this.skilltitlelists.get(id);
	}

	public Collection<SkillTitleListConfig> list() {
		return this.skilltitlelists.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = SkillTitleListManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, SkillTitleListConfig> map = new HashMap<>();
			for (Element e : list) {
				SkillTitleListConfig skilltitlelist = new SkillTitleListConfig(e);
				map.put(skilltitlelist.getId(), skilltitlelist);
			}
			
			this.skilltitlelists = map;

			log.info("load SkillTitleListConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load SkillTitleListConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
