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

import cn.game.protocol.generated.config.OccupationLevelTreeConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class OccupationLevelTreeManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(OccupationLevelTreeManager.class);

	private static OccupationLevelTreeManager instance = new OccupationLevelTreeManager();
	private static final String xmlFileName = "OccupationLevelTree";
	
	private Map<Integer, OccupationLevelTreeConfig> occupationleveltrees = new HashMap<>();
	private Map<Integer,List<OccupationLevelTreeConfig>> roleIds = new HashMap<>();

	public static OccupationLevelTreeManager getInstance() {
		return instance;
	}

	private OccupationLevelTreeManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public OccupationLevelTreeConfig getOccupationLevelTreeConfig(int id) {
		OccupationLevelTreeConfig config = this.occupationleveltrees.get(id);
		if (config == null) { 
			throw new NullPointerException("【OccupationLevelTree】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public OccupationLevelTreeConfig getOccupationLevelTreeConfigNullable(int id) {
		return this.occupationleveltrees.get(id);
	}

	public List<OccupationLevelTreeConfig> getRoleIdList(int roleId) {
		return this.roleIds.get(roleId);
	}
	public Collection<OccupationLevelTreeConfig> list() {
		return this.occupationleveltrees.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = OccupationLevelTreeManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, OccupationLevelTreeConfig> occupationleveltrees = new HashMap<>();
			Map<Integer, List<OccupationLevelTreeConfig>> roleIds = new HashMap<>();
			for (Element e : list) {
				OccupationLevelTreeConfig occupationleveltree = new OccupationLevelTreeConfig(e);
				List<OccupationLevelTreeConfig> roleIdList = roleIds.get(occupationleveltree.getRoleId()); 
				if (roleIdList == null){
					roleIdList = new ArrayList<OccupationLevelTreeConfig>(2) ; 
					roleIds.put(occupationleveltree.getRoleId() ,roleIdList) ; 
				}
				roleIdList.add(occupationleveltree) ;
				occupationleveltrees.put(occupationleveltree.getId(), occupationleveltree);
			}			

			this.roleIds = com.google.common.collect.ImmutableMap.copyOf(roleIds);			
			this.occupationleveltrees = com.google.common.collect.ImmutableMap.copyOf(occupationleveltrees);

			log.info("load OccupationLevelTreeConfig size[{}]", occupationleveltrees.size());

		} catch (Exception e) {
			throw new RuntimeException("load OccupationLevelTreeConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
