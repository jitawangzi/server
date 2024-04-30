package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.SevenDaysSigninConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class SevenDaysSigninManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(SevenDaysSigninManager.class);

	private static SevenDaysSigninManager instance = new SevenDaysSigninManager();
	private static final String xmlFileName = "SevenDaysSignin";
	
	/** 总数据，按id取值 */
	private Map<Integer, SevenDaysSigninConfig> sevendayssignins = new HashMap<>();

	public static SevenDaysSigninManager instance() {
		return instance;
	}
	private SevenDaysSigninManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public SevenDaysSigninConfig get(int id) {
		SevenDaysSigninConfig config = this.sevendayssignins.get(id);
		if (config == null) { 
			throw new NullPointerException("【SevenDaysSignin】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public SevenDaysSigninConfig getNullable(int id) {
		return this.sevendayssignins.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<SevenDaysSigninConfig> list() {
		return this.sevendayssignins.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, SevenDaysSigninConfig> sevendayssignins = new HashMap<>();
			for (Element e : list) {
				SevenDaysSigninConfig sevendayssignin = new SevenDaysSigninConfig(e);
				SevenDaysSigninConfig old = sevendayssignins.put(sevendayssignin.ID, sevendayssignin);
				if (old != null) {
					throw new IllegalArgumentException("[SevenDaysSigninConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.sevendayssignins = com.google.common.collect.ImmutableMap.copyOf(sevendayssignins);

			log.info("load SevenDaysSigninConfig size[{}]", sevendayssignins.size());

		} catch (Exception e) {
			throw new RuntimeException("load SevenDaysSigninConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
