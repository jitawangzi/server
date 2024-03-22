package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.SignInPrizeConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class SignInPrizeManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(SignInPrizeManager.class);

	private static SignInPrizeManager instance = new SignInPrizeManager();
	private static final String xmlFileName = "SignInPrize";
	
	private Map<Integer, SignInPrizeConfig> signinprizes = new HashMap<>();

	public static SignInPrizeManager getInstance() {
		return instance;
	}

	private SignInPrizeManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public SignInPrizeConfig getSignInPrizeConfig(int id) {
		SignInPrizeConfig config = this.signinprizes.get(id);
		if (config == null) { 
			throw new NullPointerException("【SignInPrize】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public SignInPrizeConfig getSignInPrizeConfigNullable(int id) {
		return this.signinprizes.get(id);
	}

	public Collection<SignInPrizeConfig> list() {
		return this.signinprizes.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = SignInPrizeManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, SignInPrizeConfig> signinprizes = new HashMap<>();
			for (Element e : list) {
				SignInPrizeConfig signinprize = new SignInPrizeConfig(e);
				signinprizes.put(signinprize.getId(), signinprize);
			}			

			this.signinprizes = com.google.common.collect.ImmutableMap.copyOf(signinprizes);

			log.info("load SignInPrizeConfig size[{}]", signinprizes.size());

		} catch (Exception e) {
			throw new RuntimeException("load SignInPrizeConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
