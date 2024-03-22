package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ExpConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class ExpManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ExpManager.class);

	private static ExpManager instance = new ExpManager();
	private static final String xmlFileName = "Exp";
	
	/** 总数据，按id取值 */
	private Map<Integer, ExpConfig> exps = new HashMap<>();

	public static ExpManager instance() {
		return instance;
	}
	private ExpManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public ExpConfig get(int id) {
		ExpConfig config = this.exps.get(id);
		if (config == null) { 
			throw new NullPointerException("【Exp】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public ExpConfig getNullable(int id) {
		return this.exps.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<ExpConfig> list() {
		return this.exps.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, ExpConfig> exps = new HashMap<>();
			for (Element e : list) {
				ExpConfig exp = new ExpConfig(e);
				ExpConfig old = exps.put(exp.ID, exp);
				if (old != null) {
					throw new IllegalArgumentException("[ExpConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.exps = com.google.common.collect.ImmutableMap.copyOf(exps);

			log.info("load ExpConfig size[{}]", exps.size());

		} catch (Exception e) {
			throw new RuntimeException("load ExpConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
