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

import cn.game.protocol.generated.config.MissionChallengeGroupConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class MissionChallengeGroupManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(MissionChallengeGroupManager.class);

	private static MissionChallengeGroupManager instance = new MissionChallengeGroupManager();
	private static final String xmlFileName = "MissionChallengeGroup";
	
	private Map<Integer, MissionChallengeGroupConfig> missionchallengegroups = new HashMap<>();
	private Map<Integer,List<MissionChallengeGroupConfig>> types = new HashMap<>();

	public static MissionChallengeGroupManager getInstance() {
		return instance;
	}

	private MissionChallengeGroupManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public MissionChallengeGroupConfig getMissionChallengeGroupConfig(int id) {
		MissionChallengeGroupConfig config = this.missionchallengegroups.get(id);
		if (config == null) { 
			throw new NullPointerException("【MissionChallengeGroup】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public MissionChallengeGroupConfig getMissionChallengeGroupConfigNullable(int id) {
		return this.missionchallengegroups.get(id);
	}

	public List<MissionChallengeGroupConfig> getTypeList(int type) {
		return this.types.get(type);
	}
	public Collection<MissionChallengeGroupConfig> list() {
		return this.missionchallengegroups.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = MissionChallengeGroupManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, MissionChallengeGroupConfig> missionchallengegroups = new HashMap<>();
			Map<Integer, List<MissionChallengeGroupConfig>> types = new HashMap<>();
			for (Element e : list) {
				MissionChallengeGroupConfig missionchallengegroup = new MissionChallengeGroupConfig(e);
				List<MissionChallengeGroupConfig> typeList = types.get(missionchallengegroup.getType()); 
				if (typeList == null){
					typeList = new ArrayList<MissionChallengeGroupConfig>(2) ; 
					types.put(missionchallengegroup.getType() ,typeList) ; 
				}
				typeList.add(missionchallengegroup) ;
				missionchallengegroups.put(missionchallengegroup.getId(), missionchallengegroup);
			}			

			this.types = com.google.common.collect.ImmutableMap.copyOf(types);			
			this.missionchallengegroups = com.google.common.collect.ImmutableMap.copyOf(missionchallengegroups);

			log.info("load MissionChallengeGroupConfig size[{}]", missionchallengegroups.size());

		} catch (Exception e) {
			throw new RuntimeException("load MissionChallengeGroupConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
