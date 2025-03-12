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
	int loadDataDistributed(Class<? extends GenericDataLoader> loaderClass, long lastId, int limit);

	Future<Integer> zongmenBargainPrice(long zongmenId);

	Future<Boolean> buyZongmenBargain(long zongmenId, long playerId);

}
