package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.TeamConfigureConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class TeamConfigureManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(TeamConfigureManager.class);

	private static TeamConfigureManager instance = new TeamConfigureManager();
	private static final String xmlFileName = "TeamConfigure";
	
	private Map<Integer, TeamConfigureConfig> teamconfigures = new HashMap<>();

	public static TeamConfigureManager getInstance() {
		return instance;
	}

	private TeamConfigureManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public TeamConfigureConfig getTeamConfigureConfig(int id) {
		TeamConfigureConfig config = this.teamconfigures.get(id);
		if (config == null) { 
			throw new NullPointerException("【TeamConfigure】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public TeamConfigureConfig getTeamConfigureConfigNullable(int id) {
		return this.teamconfigures.get(id);
	}

	public Collection<TeamConfigureConfig> list() {
		return this.teamconfigures.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = TeamConfigureManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, TeamConfigureConfig> teamconfigures = new HashMap<>();
			for (Element e : list) {
				TeamConfigureConfig teamconfigure = new TeamConfigureConfig(e);
				teamconfigures.put(teamconfigure.getId(), teamconfigure);
			}			

			this.teamconfigures = com.google.common.collect.ImmutableMap.copyOf(teamconfigures);

			log.info("load TeamConfigureConfig size[{}]", teamconfigures.size());

		} catch (Exception e) {
			throw new RuntimeException("load TeamConfigureConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
