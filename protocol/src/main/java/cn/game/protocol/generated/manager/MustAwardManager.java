package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.MustAwardConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class MustAwardManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(MustAwardManager.class);

	private static MustAwardManager instance = new MustAwardManager();
	private static final String xmlFileName = "MustAward";
	
	private Map<Integer, MustAwardConfig> mustawards = new HashMap<>();

	public static MustAwardManager getInstance() {
		return instance;
	}

	private MustAwardManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public MustAwardConfig get(int id) {
		MustAwardConfig config = this.mustawards.get(id);
		if (config == null) { 
			throw new NullPointerException("【MustAward】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public MustAwardConfig getNullable(int id) {
		return this.mustawards.get(id);
	}

	public Collection<MustAwardConfig> list() {
		return this.mustawards.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, MustAwardConfig> mustawards = new HashMap<>();
			for (Element e : list) {
				MustAwardConfig mustaward = new MustAwardConfig(e);
				mustawards.put(mustaward.getID(), mustaward);
			}			

			this.mustawards = com.google.common.collect.ImmutableMap.copyOf(mustawards);

			log.info("load MustAwardConfig size[{}]", mustawards.size());

		} catch (Exception e) {
			throw new RuntimeException("load MustAwardConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
