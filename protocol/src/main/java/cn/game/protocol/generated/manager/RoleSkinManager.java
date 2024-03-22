package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.RoleSkinConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RoleSkinManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RoleSkinManager.class);

	private static RoleSkinManager instance = new RoleSkinManager();
	private static final String xmlFileName = "RoleSkin";
	
	private Map<Integer, RoleSkinConfig> roleskins = new HashMap<>();

	public static RoleSkinManager getInstance() {
		return instance;
	}

	private RoleSkinManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public RoleSkinConfig getRoleSkinConfig(int id) {
		RoleSkinConfig config = this.roleskins.get(id);
		if (config == null) { 
			throw new NullPointerException("【RoleSkin】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public RoleSkinConfig getRoleSkinConfigNullable(int id) {
		return this.roleskins.get(id);
	}

	public Collection<RoleSkinConfig> list() {
		return this.roleskins.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = RoleSkinManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, RoleSkinConfig> roleskins = new HashMap<>();
			for (Element e : list) {
				RoleSkinConfig roleskin = new RoleSkinConfig(e);
				roleskins.put(roleskin.getId(), roleskin);
			}			

			this.roleskins = com.google.common.collect.ImmutableMap.copyOf(roleskins);

			log.info("load RoleSkinConfig size[{}]", roleskins.size());

		} catch (Exception e) {
			throw new RuntimeException("load RoleSkinConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
