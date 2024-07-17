package cn.game.games.net.data.remote;

import java.util.List;

import cn.game.games.net.game.db.DbTask;
import io.vertx.core.Future;

/**
 * @Description DataServer提供给GameServer调用的远程接口
 * 2020年11月2日 下午12:55:18
 * @author SYQ
 */
public interface DataGameServerInterface {

	/**
	 * @Description 执行数据库操作
	 * @param mapperClass
	 *            mybatis的Mapper class
	 * @param method
	 *            方法名
	 * @param args
	 *            参数
	 * @return
	 */
	public Object exec(Class<?> mapperClass, String method, Object args);

	/**
	 * 异步数据库操作
	 * 
	 * @param mapperClass
	 * @param method
	 * @param args
	 * @return
	 */
	public Future<?> execAsync(Class<?> mapperClass, String method, Object args);

	public Future<List<Object>> execAsync(List<DbTask> tasks);

	/**
	 * @Description 一次性执行多个数据库操作
	 * @param tasks
	 * @return
	 */
	public Object execMutiTasks(List<DbTask> tasks);

	void shutdown();

}
