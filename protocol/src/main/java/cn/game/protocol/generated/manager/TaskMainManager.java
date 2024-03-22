package cn.game.protocol.generated.manager;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.TaskMainConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

public class TaskMainManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(TaskMainManager.class);

	private static TaskMainManager instance = new TaskMainManager();
	public static final String xmlFileName = "TaskMain";
	
	private Map<Integer, TaskMainConfig> taskmains = new HashMap<>();

	public static TaskMainManager getInstance() {
		return instance;
	}

	private TaskMainManager() {
		WatchServiceManager.getInstance().register(this);
	}
	public TaskMainConfig getTaskMainConfig(int id) {
		return this.taskmains.get(id);
	}

	public Collection<TaskMainConfig> list() {
		return this.taskmains.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = TaskMainManager.class.getClassLoader();
			}
			URL url = classLoader.getResource("xml/" + xmlFileName + ".xml");
			Document document = XmlUtils.load(url.getPath());
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, TaskMainConfig> map = new HashMap<>();
			for (Element e : list) {
				TaskMainConfig taskmain = new TaskMainConfig(e);
				map.put(taskmain.getId(), taskmain);
			}
			
			this.taskmains = map;

			log.info("load TaskMainConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load TaskMainConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
