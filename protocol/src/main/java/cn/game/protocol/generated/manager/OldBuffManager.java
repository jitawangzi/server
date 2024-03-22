package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.OldBuffConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class OldBuffManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(OldBuffManager.class);

	private static OldBuffManager instance = new OldBuffManager();
	private static final String xmlFileName = "Buff";
	
	private Map<Integer, OldBuffConfig> buffs = new HashMap<>();

	public static OldBuffManager getInstance() {
		return instance;
	}

	private OldBuffManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public OldBuffConfig getBuffConfig(int id) {
		OldBuffConfig config = this.buffs.get(id);
		if (config == null) { 
			throw new NullPointerException("【Buff】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public OldBuffConfig getBuffConfigNullable(int id) {
		return this.buffs.get(id);
	}

	public Collection<OldBuffConfig> list() {
		return this.buffs.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = OldBuffManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, OldBuffConfig> buffs = new HashMap<>();
			for (Element e : list) {
				OldBuffConfig buff = new OldBuffConfig(e);
				buffs.put(buff.getId(), buff);
			}			

			this.buffs = com.google.common.collect.ImmutableMap.copyOf(buffs);

			log.info("load BuffConfig size[{}]", buffs.size());

		} catch (Exception e) {
			throw new RuntimeException("load BuffConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
