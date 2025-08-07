package cn.game.games.net.cross.remote;


import cn.game.core.db.GenericDataLoader;
import cn.game.core.net.remote.RemoteCrossServerInterface;
import io.vertx.core.Future;

/**    
 * Cross提供给Game、Cross服务器调用的远程接口
 * 2025年1月22日 15:53:20
 * @author SYQ
 */
public interface CrossServerInterface extends RemoteCrossServerInterface {

	/**
	 * 加载分布式数据
	 * @param loaderClass 数据加载器类
	 * @param lastId 最后一次查询的id，分页参数
	 * @param limit 数据数量，分页参数
	 * @return 加载数量
	 */
	<T, ID extends Number> int loadDataDistributed(Class<? extends GenericDataLoader<T, ID>> loaderClass, ID lastId, int limit);

	/** 
	 * 加载某id的数据
	 * @param <T>
	 * @param <ID>
	 * @param loaderClass
	 * @param id
	 * @return
	 */
	<T, ID extends Number> int loadDataDistributed(Class<? extends GenericDataLoader<T, ID>> loaderClass, ID id);

}
