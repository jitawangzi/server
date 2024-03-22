package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.FriendlyConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class FriendlyManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(FriendlyManager.class);

	private static FriendlyManager instance = new FriendlyManager();
	private static final String xmlFileName = "Friendly";
	
	private Map<Integer, FriendlyConfig> friendlys = new HashMap<>();

	public static FriendlyManager getInstance() {
		return instance;
	}

	private FriendlyManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public FriendlyConfig getFriendlyConfig(int id) {
		FriendlyConfig config = this.friendlys.get(id);
		if (config == null) { 
			throw new NullPointerException("【Friendly】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public FriendlyConfig getFriendlyConfigNullable(int id) {
		return this.friendlys.get(id);
	}

	public Collection<FriendlyConfig> list() {
		return this.friendlys.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = FriendlyManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, FriendlyConfig> friendlys = new HashMap<>();
			for (Element e : list) {
				FriendlyConfig friendly = new FriendlyConfig(e);
				friendlys.put(friendly.getId(), friendly);
			}			

			this.friendlys = com.google.common.collect.ImmutableMap.copyOf(friendlys);

			log.info("load FriendlyConfig size[{}]", friendlys.size());

		} catch (Exception e) {
			throw new RuntimeException("load FriendlyConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
