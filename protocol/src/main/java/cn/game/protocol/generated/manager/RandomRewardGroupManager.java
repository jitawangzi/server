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

import cn.game.protocol.generated.config.RandomRewardGroupConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RandomRewardGroupManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RandomRewardGroupManager.class);

	private static RandomRewardGroupManager instance = new RandomRewardGroupManager();
	private static final String xmlFileName = "RandomRewardGroup";
	
	private Map<Integer,List<RandomRewardGroupConfig>> groupIds = new HashMap<>();

	public static RandomRewardGroupManager getInstance() {
		return instance;
	}

	private RandomRewardGroupManager() {
		WatchServiceManager.getInstance().register(this);
	}

	public List<RandomRewardGroupConfig> getGroupIdList(int groupId) {
		return this.groupIds.get(groupId);
	}

	public Collection<List<RandomRewardGroupConfig>> list() {
		return groupIds.values();
	}


	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = RandomRewardGroupManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, List<RandomRewardGroupConfig>> groupIds = new HashMap<>();
			for (Element e : list) {
				RandomRewardGroupConfig randomrewardgroup = new RandomRewardGroupConfig(e);
				List<RandomRewardGroupConfig> groupIdList = groupIds.get(randomrewardgroup.getGroupId()); 
				if (groupIdList == null){
					groupIdList = new ArrayList<RandomRewardGroupConfig>(2) ; 
					groupIds.put(randomrewardgroup.getGroupId() ,groupIdList) ; 
				}
				groupIdList.add(randomrewardgroup) ;
			}			

			this.groupIds = com.google.common.collect.ImmutableMap.copyOf(groupIds);			

			log.info("load RandomRewardGroupConfig size[{}]", groupIds.size());

		} catch (Exception e) {
			throw new RuntimeException("load RandomRewardGroupConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
