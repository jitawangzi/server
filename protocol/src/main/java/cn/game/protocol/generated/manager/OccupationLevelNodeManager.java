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

import cn.game.protocol.generated.config.OccupationLevelNodeConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class OccupationLevelNodeManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(OccupationLevelNodeManager.class);

	private static OccupationLevelNodeManager instance = new OccupationLevelNodeManager();
	private static final String xmlFileName = "OccupationLevelNode";
	
	private Map<Integer, OccupationLevelNodeConfig> occupationlevelnodes = new HashMap<>();
	private Map<Integer,List<OccupationLevelNodeConfig>> groupsIds = new HashMap<>();

	public static OccupationLevelNodeManager getInstance() {
		return instance;
	}

	private OccupationLevelNodeManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public OccupationLevelNodeConfig getOccupationLevelNodeConfig(int id) {
		OccupationLevelNodeConfig config = this.occupationlevelnodes.get(id);
		if (config == null) { 
			throw new NullPointerException("【OccupationLevelNode】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public OccupationLevelNodeConfig getOccupationLevelNodeConfigNullable(int id) {
		return this.occupationlevelnodes.get(id);
	}

	public List<OccupationLevelNodeConfig> getGroupsIdList(int groupsId) {
		return this.groupsIds.get(groupsId);
	}
	public Collection<OccupationLevelNodeConfig> list() {
		return this.occupationlevelnodes.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = OccupationLevelNodeManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, OccupationLevelNodeConfig> occupationlevelnodes = new HashMap<>();
			Map<Integer, List<OccupationLevelNodeConfig>> groupsIds = new HashMap<>();
			for (Element e : list) {
				OccupationLevelNodeConfig occupationlevelnode = new OccupationLevelNodeConfig(e);
				List<OccupationLevelNodeConfig> groupsIdList = groupsIds.get(occupationlevelnode.getGroupsId()); 
				if (groupsIdList == null){
					groupsIdList = new ArrayList<OccupationLevelNodeConfig>(2) ; 
					groupsIds.put(occupationlevelnode.getGroupsId() ,groupsIdList) ; 
				}
				groupsIdList.add(occupationlevelnode) ;
				occupationlevelnodes.put(occupationlevelnode.getId(), occupationlevelnode);
			}			

			this.groupsIds = com.google.common.collect.ImmutableMap.copyOf(groupsIds);			
			this.occupationlevelnodes = com.google.common.collect.ImmutableMap.copyOf(occupationlevelnodes);

			log.info("load OccupationLevelNodeConfig size[{}]", occupationlevelnodes.size());

		} catch (Exception e) {
			throw new RuntimeException("load OccupationLevelNodeConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
