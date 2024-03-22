package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.TeamExpConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class TeamExpManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(TeamExpManager.class);

	private static TeamExpManager instance = new TeamExpManager();
	private static final String xmlFileName = "TeamExp";
	
	private Map<Integer, TeamExpConfig> teamexps = new HashMap<>();

	public static TeamExpManager getInstance() {
		return instance;
	}

	private TeamExpManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public TeamExpConfig getTeamExpConfig(int id) {
		TeamExpConfig config = this.teamexps.get(id);
		if (config == null) { 
			throw new NullPointerException("【TeamExp】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public TeamExpConfig getTeamExpConfigNullable(int id) {
		return this.teamexps.get(id);
	}

	public Collection<TeamExpConfig> list() {
		return this.teamexps.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = TeamExpManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, TeamExpConfig> teamexps = new HashMap<>();
			for (Element e : list) {
				TeamExpConfig teamexp = new TeamExpConfig(e);
				teamexps.put(teamexp.getId(), teamexp);
			}			

			this.teamexps = com.google.common.collect.ImmutableMap.copyOf(teamexps);

			log.info("load TeamExpConfig size[{}]", teamexps.size());

		} catch (Exception e) {
			throw new RuntimeException("load TeamExpConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
