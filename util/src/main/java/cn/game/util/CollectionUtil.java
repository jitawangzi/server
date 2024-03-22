package cn.game.util;
import java.util.Collection;

/** 
* 
* @date 2023年3月28日 上午11:18:01 
* @author YYB 
*/
public class CollectionUtil {

	/**
	 * 集合同时包含元素
	 * @param collection
	 * @param o1
	 * @param o2
	 * @return
	 */
	public static boolean contains(Collection<?> collection, Object... objs) {
		boolean contains = true;
		for (Object o : objs) {
			contains = collection.contains(o);
			if (!contains) {
				return contains;
			}
		}
		
		return contains;
	}
	
}
