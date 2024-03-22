package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.RoleConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RoleManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RoleManager.class);

	private static RoleManager instance = new RoleManager();
	private static final String xmlFileName = "Role";
	
	private Map<Integer, RoleConfig> roles = new HashMap<>();
	private Map<Integer,List<RoleConfig>> types = new HashMap<>();

	public static RoleManager getInstance() {
		return instance;
	}

	private RoleManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public RoleConfig getRoleConfig(int id) {
		RoleConfig config = this.roles.get(id);
		if (config == null) { 
			throw new NullPointerException("【Role】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public RoleConfig getRoleConfigNullable(int id) {
		return this.roles.get(id);
	}

	public List<RoleConfig> getTypeList(int type) {
		return this.types.get(type);
	}
	public Collection<RoleConfig> list() {
		return this.roles.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = RoleManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, RoleConfig> roles = new HashMap<>();
			Map<Integer, List<RoleConfig>> types = new HashMap<>();
			for (Element e : list) {
				RoleConfig role = new RoleConfig(e);
				List<RoleConfig> typeList = types.get(role.getType()); 
				if (typeList == null){
					typeList = new ArrayList<RoleConfig>(2) ; 
					types.put(role.getType() ,typeList) ; 
				}
				typeList.add(role) ;
				roles.put(role.getId(), role);
			}			

			this.types = com.google.common.collect.ImmutableMap.copyOf(types);			
			this.roles = com.google.common.collect.ImmutableMap.copyOf(roles);

			log.info("load RoleConfig size[{}]", roles.size());

		} catch (Exception e) {
			throw new RuntimeException("load RoleConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
