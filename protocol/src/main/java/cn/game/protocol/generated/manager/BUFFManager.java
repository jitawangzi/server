package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.BUFFConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class BUFFManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(BUFFManager.class);

	private static BUFFManager instance = new BUFFManager();
	private static final String xmlFileName = "BUFF";
	
	/** 总数据，按id取值 */
	private Map<Integer, BUFFConfig> buffs = new HashMap<>();

	public static BUFFManager instance() {
		return instance;
	}
	private BUFFManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public BUFFConfig get(int id) {
		BUFFConfig config = this.buffs.get(id);
		if (config == null) { 
			throw new NullPointerException("【BUFF】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public BUFFConfig getNullable(int id) {
		return this.buffs.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<BUFFConfig> list() {
		return this.buffs.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, BUFFConfig> buffs = new HashMap<>();
			for (Element e : list) {
				BUFFConfig buff = new BUFFConfig(e);
				BUFFConfig old = buffs.put(buff.ID, buff);
				if (old != null) {
					throw new IllegalArgumentException("[BUFFConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.buffs = com.google.common.collect.ImmutableMap.copyOf(buffs);

			log.info("load BUFFConfig size[{}]", buffs.size());

		} catch (Exception e) {
			throw new RuntimeException("load BUFFConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
