package cn.game.games.net.cross.remote;


import cn.game.core.db.GenericDataLoader;
import cn.game.core.net.remote.RemoteCrossServerInterface;

/**    
 * Cross提供给Game、Cross服务器调用的远程接口
 * 2025年1月22日 15:53:20
 * @author SYQ
 */
public interface CrossServerInterface extends RemoteCrossServerInterface {

	/**
	 * 加载分布式数据
	 * @param loaderClass 数据加载器类
	 * @param offset 数据偏移
	 * @param limit 数据数量
	 * @return 加载数量
	 */
	int loadDataDistributed(Class<? extends GenericDataLoader> loaderClass, int offset, int limit);

	io.vertx.core.Future<Integer> zongmenBargainPrice(long zongmenId);

	io.vertx.core.Future<Boolean> buyZongmenBargain(long zongmenId, long playerId);

}
