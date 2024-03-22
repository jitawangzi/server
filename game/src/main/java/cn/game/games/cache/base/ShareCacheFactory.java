package cn.game.games.cache.base;

import java.util.HashMap;
import java.util.Map;


/**
 * 共享缓存op工厂
 * @author abc
 *
 */
public class ShareCacheFactory {

	//cache class => cache op
	private static Map<Class<?>, ICacheOp> instances = new HashMap<>();
	
	/**
	 * 缓存操作实现类
	 * 
	 * @param opImplClass
	 * @return
	 */
	public static <T> T getCache(Class<? extends BaseShareOp> opImplClass){
		ICacheOp op = instances.get(opImplClass);
		if(op == null){
			try {
				op = opImplClass.newInstance();
				op.init();
			} catch (InstantiationException | IllegalAccessException e) {				
				throw new RuntimeException(e);
			}
			instances.put(opImplClass, op);
		}
		return (T)op;
	}
}
