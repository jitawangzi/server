package cn.game.login.cache.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.cache.CacheType;


public class BaseCache {
	
	private Logger log;
	
	public void init(){
		log = LoggerFactory.getLogger(this.getClass());
//		ct = LoginContext.getInstance().getRedisTemplate();
		
	}
	
	/**
	 * 计算缓存的健值
	 * 
	 * @param type 缓存类型
	 * @return 最终的缓存key
	 */
	public String key(CacheType type) {
		return type.key();
	}
	
//	public void setCt(XConsistentTemplate ct) {
//		this.ct = ct;
//	}

	 
}
