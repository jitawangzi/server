package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.RoutineTrainingConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RoutineTrainingManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RoutineTrainingManager.class);

	private static RoutineTrainingManager instance = new RoutineTrainingManager();
	private static final String xmlFileName = "RoutineTraining";
	
	private Map<Integer, RoutineTrainingConfig> routinetrainings = new HashMap<>();

	public static RoutineTrainingManager getInstance() {
		return instance;
	}

	private RoutineTrainingManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public RoutineTrainingConfig getRoutineTrainingConfig(int id) {
		RoutineTrainingConfig config = this.routinetrainings.get(id);
		if (config == null) { 
			throw new NullPointerException("【RoutineTraining】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public RoutineTrainingConfig getRoutineTrainingConfigNullable(int id) {
		return this.routinetrainings.get(id);
	}

	public Collection<RoutineTrainingConfig> list() {
		return this.routinetrainings.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = RoutineTrainingManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, RoutineTrainingConfig> routinetrainings = new HashMap<>();
			for (Element e : list) {
				RoutineTrainingConfig routinetraining = new RoutineTrainingConfig(e);
				routinetrainings.put(routinetraining.getId(), routinetraining);
			}			

			this.routinetrainings = com.google.common.collect.ImmutableMap.copyOf(routinetrainings);

			log.info("load RoutineTrainingConfig size[{}]", routinetrainings.size());

		} catch (Exception e) {
			throw new RuntimeException("load RoutineTrainingConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
