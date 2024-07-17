package cn.game.games.net.game.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description 针对全部玩家的邮件，可能也用不到
 * 2020年10月15日 下午2:30:40
 * @author SYQ
 */
public class GlobalMailManager {

	private static final Logger log = LoggerFactory.getLogger(GlobalMailManager.class);

	private static GlobalMailManager instance = new GlobalMailManager() ; 

	public static GlobalMailManager getInstance() {
		return instance ; 
	}

}
