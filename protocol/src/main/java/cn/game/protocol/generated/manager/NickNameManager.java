package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.NickNameConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class NickNameManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(NickNameManager.class);

	private static NickNameManager instance = new NickNameManager();
	private static final String xmlFileName = "NickName";
	
	private Map<Integer, NickNameConfig> nicknames = new HashMap<>();

	public static NickNameManager getInstance() {
		return instance;
	}

	private NickNameManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public NickNameConfig get(int id) {
		NickNameConfig config = this.nicknames.get(id);
		if (config == null) { 
			throw new NullPointerException("【NickName】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public NickNameConfig getNullable(int id) {
		return this.nicknames.get(id);
	}

	public Collection<NickNameConfig> list() {
		return this.nicknames.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, NickNameConfig> nicknames = new HashMap<>();
			for (Element e : list) {
				NickNameConfig nickname = new NickNameConfig(e);
				nicknames.put(nickname.getID(), nickname);
			}			

			this.nicknames = com.google.common.collect.ImmutableMap.copyOf(nicknames);

			log.info("load NickNameConfig size[{}]", nicknames.size());

		} catch (Exception e) {
			throw new RuntimeException("load NickNameConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
