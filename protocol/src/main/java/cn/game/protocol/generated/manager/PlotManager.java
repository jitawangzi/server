package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.PlotConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class PlotManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(PlotManager.class);

	private static PlotManager instance = new PlotManager();
	private static final String xmlFileName = "Plot";
	
	/** 总数据，按id取值 */
	private Map<Integer, PlotConfig> plots = new HashMap<>();

	public static PlotManager instance() {
		return instance;
	}
	private PlotManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public PlotConfig get(int id) {
		PlotConfig config = this.plots.get(id);
		if (config == null) { 
			throw new NullPointerException("【Plot】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public PlotConfig getNullable(int id) {
		return this.plots.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<PlotConfig> list() {
		return this.plots.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, PlotConfig> plots = new HashMap<>();
			for (Element e : list) {
				PlotConfig plot = new PlotConfig(e);
				PlotConfig old = plots.put(plot.ID, plot);
				if (old != null) {
					throw new IllegalArgumentException("[PlotConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.plots = com.google.common.collect.ImmutableMap.copyOf(plots);

			log.info("load PlotConfig size[{}]", plots.size());

		} catch (Exception e) {
			throw new RuntimeException("load PlotConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
