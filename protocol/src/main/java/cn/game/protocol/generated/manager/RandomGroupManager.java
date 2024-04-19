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

import cn.game.protocol.generated.config.RandomGroupConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RandomGroupManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RandomGroupManager.class);

	private static RandomGroupManager instance = new RandomGroupManager();
	private static final String xmlFileName = "RandomGroup";
	
	/** 普通索引 */
	private Map<Integer,List<RandomGroupConfig>> RandomGroupIDs = new HashMap<>();

	public static RandomGroupManager instance() {
		return instance;
	}
	private RandomGroupManager() {
		WatchServiceManager.getInstance().register(this);
	}

	public List<RandomGroupConfig> getRandomGroupIDList(int RandomGroupID) {
		return this.RandomGroupIDs.get(RandomGroupID);
	}
	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<List<RandomGroupConfig>> list() {
		return RandomGroupIDs.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, List<RandomGroupConfig>> RandomGroupIDs = new HashMap<>();
			for (Element e : list) {
				RandomGroupConfig randomgroup = new RandomGroupConfig(e);
				List<RandomGroupConfig> RandomGroupIDList = RandomGroupIDs.get(randomgroup.RandomGroupID); 
				if (RandomGroupIDList == null){
					RandomGroupIDList = new ArrayList<RandomGroupConfig>(2) ; 
					RandomGroupIDs.put(randomgroup.RandomGroupID ,RandomGroupIDList) ; 
				}
				RandomGroupIDList.add(randomgroup) ;
			}			

			this.RandomGroupIDs = com.google.common.collect.ImmutableMap.copyOf(RandomGroupIDs);			

			log.info("load RandomGroupConfig size[{}]", RandomGroupIDs.values().stream().flatMap(List::stream).count());

		} catch (Exception e) {
			throw new RuntimeException("load RandomGroupConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
