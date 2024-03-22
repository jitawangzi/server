package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.MechaConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class MechaManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(MechaManager.class);

	private static MechaManager instance = new MechaManager();
	private static final String xmlFileName = "Mecha";
	
	private Map<Integer, MechaConfig> mechas = new HashMap<>();

	public static MechaManager getInstance() {
		return instance;
	}

	private MechaManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public MechaConfig getMechaConfig(int id) {
		MechaConfig config = this.mechas.get(id);
		if (config == null) { 
			throw new NullPointerException("【Mecha】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public MechaConfig getMechaConfigNullable(int id) {
		return this.mechas.get(id);
	}

	public Collection<MechaConfig> list() {
		return this.mechas.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = MechaManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, MechaConfig> mechas = new HashMap<>();
			for (Element e : list) {
				MechaConfig mecha = new MechaConfig(e);
				mechas.put(mecha.getId(), mecha);
			}			

			this.mechas = com.google.common.collect.ImmutableMap.copyOf(mechas);

			log.info("load MechaConfig size[{}]", mechas.size());

		} catch (Exception e) {
			throw new RuntimeException("load MechaConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
