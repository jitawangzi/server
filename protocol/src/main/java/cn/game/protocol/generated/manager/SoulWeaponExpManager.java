package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.SoulWeaponExpConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class SoulWeaponExpManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(SoulWeaponExpManager.class);

	private static SoulWeaponExpManager instance = new SoulWeaponExpManager();
	private static final String xmlFileName = "SoulWeaponExp";
	
	private Map<Integer, SoulWeaponExpConfig> soulweaponexps = new HashMap<>();

	public static SoulWeaponExpManager getInstance() {
		return instance;
	}

	private SoulWeaponExpManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public SoulWeaponExpConfig getSoulWeaponExpConfig(int id) {
		SoulWeaponExpConfig config = this.soulweaponexps.get(id);
		if (config == null) { 
			throw new NullPointerException("【SoulWeaponExp】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public SoulWeaponExpConfig getSoulWeaponExpConfigNullable(int id) {
		return this.soulweaponexps.get(id);
	}

	public Collection<SoulWeaponExpConfig> list() {
		return this.soulweaponexps.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = SoulWeaponExpManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, SoulWeaponExpConfig> soulweaponexps = new HashMap<>();
			for (Element e : list) {
				SoulWeaponExpConfig soulweaponexp = new SoulWeaponExpConfig(e);
				soulweaponexps.put(soulweaponexp.getId(), soulweaponexp);
			}			

			this.soulweaponexps = com.google.common.collect.ImmutableMap.copyOf(soulweaponexps);

			log.info("load SoulWeaponExpConfig size[{}]", soulweaponexps.size());

		} catch (Exception e) {
			throw new RuntimeException("load SoulWeaponExpConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
