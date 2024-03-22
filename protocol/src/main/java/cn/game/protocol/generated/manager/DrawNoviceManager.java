package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.DrawNoviceConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class DrawNoviceManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(DrawNoviceManager.class);

	private static DrawNoviceManager instance = new DrawNoviceManager();
	private static final String xmlFileName = "DrawNovice";
	
	private Map<Integer, DrawNoviceConfig> drawnovices = new HashMap<>();

	public static DrawNoviceManager getInstance() {
		return instance;
	}

	private DrawNoviceManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public DrawNoviceConfig getDrawNoviceConfig(int id) {
		DrawNoviceConfig config = this.drawnovices.get(id);
		if (config == null) { 
			throw new NullPointerException("【DrawNovice】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public DrawNoviceConfig getDrawNoviceConfigNullable(int id) {
		return this.drawnovices.get(id);
	}

	public Collection<DrawNoviceConfig> list() {
		return this.drawnovices.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = DrawNoviceManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, DrawNoviceConfig> drawnovices = new HashMap<>();
			for (Element e : list) {
				DrawNoviceConfig drawnovice = new DrawNoviceConfig(e);
				drawnovices.put(drawnovice.getId(), drawnovice);
			}			

			this.drawnovices = com.google.common.collect.ImmutableMap.copyOf(drawnovices);

			log.info("load DrawNoviceConfig size[{}]", drawnovices.size());

		} catch (Exception e) {
			throw new RuntimeException("load DrawNoviceConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
