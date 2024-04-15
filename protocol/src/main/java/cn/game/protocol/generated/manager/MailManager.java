package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.MailConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class MailManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(MailManager.class);

	private static MailManager instance = new MailManager();
	private static final String xmlFileName = "Mail";
	
	/** 总数据，按id取值 */
	private Map<Integer, MailConfig> mails = new HashMap<>();

	public static MailManager instance() {
		return instance;
	}
	private MailManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public MailConfig get(int id) {
		MailConfig config = this.mails.get(id);
		if (config == null) { 
			throw new NullPointerException("【Mail】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public MailConfig getNullable(int id) {
		return this.mails.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<MailConfig> list() {
		return this.mails.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, MailConfig> mails = new HashMap<>();
			for (Element e : list) {
				MailConfig mail = new MailConfig(e);
				MailConfig old = mails.put(mail.ID, mail);
				if (old != null) {
					throw new IllegalArgumentException("[MailConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.mails = com.google.common.collect.ImmutableMap.copyOf(mails);

			log.info("load MailConfig size[{}]", mails.size());

		} catch (Exception e) {
			throw new RuntimeException("load MailConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
