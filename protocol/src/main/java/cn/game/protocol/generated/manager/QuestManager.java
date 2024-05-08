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

import cn.game.protocol.generated.config.QuestConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class QuestManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(QuestManager.class);

	private static QuestManager instance = new QuestManager();
	private static final String xmlFileName = "Quest";
	
	/** 总数据，按id取值 */
	private Map<Integer, QuestConfig> quests = new HashMap<>();
	/** 普通索引 */
	private Map<Integer,List<QuestConfig>> Types = new HashMap<>();

	public static QuestManager instance() {
		return instance;
	}
	private QuestManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public QuestConfig get(int id) {
		QuestConfig config = this.quests.get(id);
		if (config == null) { 
			throw new NullPointerException("【Quest】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public QuestConfig getNullable(int id) {
		return this.quests.get(id);
	}

	public List<QuestConfig> getTypeList(int Type) {
		return this.Types.get(Type);
	}
	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<QuestConfig> list() {
		return this.quests.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, QuestConfig> quests = new HashMap<>();
			Map<Integer, List<QuestConfig>> Types = new HashMap<>();
			for (Element e : list) {
				QuestConfig quest = new QuestConfig(e);
				List<QuestConfig> TypeList = Types.get(quest.Type); 
				if (TypeList == null){
					TypeList = new ArrayList<QuestConfig>(2) ; 
					Types.put(quest.Type ,TypeList) ; 
				}
				TypeList.add(quest) ;
				QuestConfig old = quests.put(quest.ID, quest);
				if (old != null) {
					throw new IllegalArgumentException("[QuestConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.Types = com.google.common.collect.ImmutableMap.copyOf(Types);			
			this.quests = com.google.common.collect.ImmutableMap.copyOf(quests);

			log.info("load QuestConfig size[{}]", quests.size());

		} catch (Exception e) {
			throw new RuntimeException("load QuestConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
