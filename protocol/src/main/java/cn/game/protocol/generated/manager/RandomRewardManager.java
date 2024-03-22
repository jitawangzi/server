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

import cn.game.protocol.generated.config.RandomRewardConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RandomRewardManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RandomRewardManager.class);

	private static RandomRewardManager instance = new RandomRewardManager();
	private static final String xmlFileName = "RandomReward";
	
	private Map<Integer,List<RandomRewardConfig>> groupIds = new HashMap<>();

	public static RandomRewardManager getInstance() {
		return instance;
	}

	private RandomRewardManager() {
		WatchServiceManager.getInstance().register(this);
	}

	public List<RandomRewardConfig> getGroupIdList(int groupId) {
		return this.groupIds.get(groupId);
	}

	public Collection<List<RandomRewardConfig>> list() {
		return groupIds.values();
	}


	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = RandomRewardManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, List<RandomRewardConfig>> groupIds = new HashMap<>();
			for (Element e : list) {
				RandomRewardConfig randomreward = new RandomRewardConfig(e);
				List<RandomRewardConfig> groupIdList = groupIds.get(randomreward.getGroupId()); 
				if (groupIdList == null){
					groupIdList = new ArrayList<RandomRewardConfig>(2) ; 
					groupIds.put(randomreward.getGroupId() ,groupIdList) ; 
				}
				groupIdList.add(randomreward) ;
			}			

			this.groupIds = com.google.common.collect.ImmutableMap.copyOf(groupIds);			

			log.info("load RandomRewardConfig size[{}]", groupIds.size());

		} catch (Exception e) {
			throw new RuntimeException("load RandomRewardConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
