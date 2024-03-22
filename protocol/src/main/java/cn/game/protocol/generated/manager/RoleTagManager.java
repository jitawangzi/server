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

import cn.game.protocol.generated.enume.RoleTagEnum;
import cn.game.protocol.generated.config.RoleTagConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RoleTagManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RoleTagManager.class);

	private static RoleTagManager instance = new RoleTagManager();
	private static final String xmlFileName = "RoleTag";
	
	private Map<Integer, RoleTagConfig> roletags = new HashMap<>();
	private Map<RoleTagEnum,List<RoleTagConfig>> type3s = new HashMap<>();
	private Map<Integer,List<RoleTagConfig>> types = new HashMap<>();

	public static RoleTagManager getInstance() {
		return instance;
	}

	private RoleTagManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public RoleTagConfig getRoleTagConfig(int id) {
		RoleTagConfig config = this.roletags.get(id);
		if (config == null) { 
			throw new NullPointerException("【RoleTag】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public RoleTagConfig getRoleTagConfigNullable(int id) {
		return this.roletags.get(id);
	}

	public List<RoleTagConfig> getType3List(RoleTagEnum type3) {
		return this.type3s.get(type3);
	}
	public List<RoleTagConfig> getTypeList(int type) {
		return this.types.get(type);
	}
	public Collection<RoleTagConfig> list() {
		return this.roletags.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = RoleTagManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, RoleTagConfig> roletags = new HashMap<>();
			Map<RoleTagEnum, List<RoleTagConfig>> type3s = new HashMap<>();
			Map<Integer, List<RoleTagConfig>> types = new HashMap<>();
			for (Element e : list) {
				RoleTagConfig roletag = new RoleTagConfig(e);
				List<RoleTagConfig> type3List = type3s.get(roletag.getType3()); 
				if (type3List == null){
					type3List = new ArrayList<RoleTagConfig>(2) ; 
					type3s.put(roletag.getType3() ,type3List) ; 
				}
				type3List.add(roletag) ;
				List<RoleTagConfig> typeList = types.get(roletag.getType()); 
				if (typeList == null){
					typeList = new ArrayList<RoleTagConfig>(2) ; 
					types.put(roletag.getType() ,typeList) ; 
				}
				typeList.add(roletag) ;
				roletags.put(roletag.getId(), roletag);
			}			

			this.type3s = com.google.common.collect.ImmutableMap.copyOf(type3s);			
			this.types = com.google.common.collect.ImmutableMap.copyOf(types);			
			this.roletags = com.google.common.collect.ImmutableMap.copyOf(roletags);

			log.info("load RoleTagConfig size[{}]", roletags.size());

		} catch (Exception e) {
			throw new RuntimeException("load RoleTagConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
