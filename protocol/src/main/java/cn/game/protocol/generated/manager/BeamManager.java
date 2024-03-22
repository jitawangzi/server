package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.BeamConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class BeamManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(BeamManager.class);

	private static BeamManager instance = new BeamManager();
	private static final String xmlFileName = "Beam";
	
	/** 总数据，按id取值 */
	private Map<Integer, BeamConfig> beams = new HashMap<>();

	public static BeamManager instance() {
		return instance;
	}
	private BeamManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public BeamConfig get(int id) {
		BeamConfig config = this.beams.get(id);
		if (config == null) { 
			throw new NullPointerException("【Beam】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public BeamConfig getNullable(int id) {
		return this.beams.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<BeamConfig> list() {
		return this.beams.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, BeamConfig> beams = new HashMap<>();
			for (Element e : list) {
				BeamConfig beam = new BeamConfig(e);
				BeamConfig old = beams.put(beam.ID, beam);
				if (old != null) {
					throw new IllegalArgumentException("[BeamConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.beams = com.google.common.collect.ImmutableMap.copyOf(beams);

			log.info("load BeamConfig size[{}]", beams.size());

		} catch (Exception e) {
			throw new RuntimeException("load BeamConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
