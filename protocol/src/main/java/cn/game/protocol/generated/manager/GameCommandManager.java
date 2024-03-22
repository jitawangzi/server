package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.GameCommandConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class GameCommandManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(GameCommandManager.class);

	private static GameCommandManager instance = new GameCommandManager();
	private static final String xmlFileName = "GameCommand";
	
	private Map<Integer, GameCommandConfig> gamecommands = new HashMap<>();

	public static GameCommandManager getInstance() {
		return instance;
	}

	private GameCommandManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public GameCommandConfig getGameCommandConfig(int id) {
		GameCommandConfig config = this.gamecommands.get(id);
		if (config == null) { 
			throw new NullPointerException("【GameCommand】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public GameCommandConfig getGameCommandConfigNullable(int id) {
		return this.gamecommands.get(id);
	}

	public Collection<GameCommandConfig> list() {
		return this.gamecommands.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = GameCommandManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, GameCommandConfig> gamecommands = new HashMap<>();
			for (Element e : list) {
				GameCommandConfig gamecommand = new GameCommandConfig(e);
				gamecommands.put(gamecommand.getId(), gamecommand);
			}			

			this.gamecommands = com.google.common.collect.ImmutableMap.copyOf(gamecommands);

			log.info("load GameCommandConfig size[{}]", gamecommands.size());

		} catch (Exception e) {
			throw new RuntimeException("load GameCommandConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
