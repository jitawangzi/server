package cn.game.games.net.data;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.games.cache.entity.PlayerData;
import cn.game.games.net.data.mapper.PlayerDataMapper;
import cn.game.util.DateUtil;
import cn.game.util.MailUtil;
import cn.game.util.ObjUtil;
import cn.game.util.SpringApolloLoader;
import cn.game.util.SpringContextLoader;

public class DataServer {

	private static Logger log = LoggerFactory.getLogger(DataServer.class);

	/** 唯一实例 */
	private static DataServer instance = new DataServer();

	public static DataServer getInstance() {
		return instance;
	}

	public static void main(String args[]) {

		try {
			SpringApolloLoader springApolloLoader = new SpringApolloLoader();
			springApolloLoader.init();
//			SpringContextLoader.main(args);

		} catch (Exception e) {
			try {
				MailUtil.reportException("Data服务器启动失败", ExceptionUtils.getFullStackTrace(e));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			System.exit(1);
		}

		log.info("数据服启动成功");
//		testDB();

	}

	public static void testDB() {
		PlayerDataMapper mapper = SpringContextLoader.getContext().getBean(PlayerDataMapper.class);

		// 如果是顺序执行，每个db操作大概50-60ms之间,100个要5s
		// 线程池执行，100个操作，2800ms，#连接池中保留的最大连接数maxPoolSize = 5
		// 同上，maxPoolSize = 50 ， 使用了800多ms，时间明显减少。或者600多
		// 同上， maxPoolSize = 500 ，结果基本一样。
		// 10 个连接创建玩家 1000 个消耗时间：8256，已经有一些超时的了
		// 100 个连接创建玩家 1000 个消耗时间：4424，时间减少了，没有超时的。
		// 1000 个连接创建玩家 1000 个消耗时间：4946/4265，和上面100个的没什么区别。估计是达到了mysql本身的连接限制

		// 用测试服务器，mysql最大连接配置为10000，连接池10000个连接创建1000个玩家，第一次用了2000多ms，后面几次都是600ms

//		TestDbInsertRequest_7f000001 request = (TestDbInsertRequest_7f000001) message;
//		int intarg = request.getTimes();

		// 创建玩家 100000 个消耗时间：63798
		// 创建玩家 10000 个消耗时间：8486

		int intarg = 10000;
		long start = System.currentTimeMillis();
		AtomicLong dbMaxPlayerId = new AtomicLong(92222110L);
		ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();

		final CountDownLatch latch = new CountDownLatch(intarg);

		for (int i = 0; i < intarg; i++) {
			final long uid = 2223000 + i;

			newCachedThreadPool.execute(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					PlayerData player = new PlayerData();
					// player.setSeq(seq) ;
					player.setGender(true);
					player.setCreateDate(DateUtil.getStringDate());
					player.setLevel(1);
					player.setUid(uid);
					player.setName(uid + "");
					player.setLoginDate(DateUtil.getStringDate());

					player.setRefreshDay(DateUtil.getDay());

					ObjUtil.setDefaultValue(player);
					long id = dbMaxPlayerId.getAndIncrement();
					player.setPlayerId(id);
					try {
						mapper.insert(player);
						latch.countDown();
					} catch (Throwable e) {
						e.printStackTrace();
						return;
					}
				}
			});

		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		log.info("创建玩家 " + intarg + " 个消耗时间：" + (System.currentTimeMillis() - start));
		System.err.println("创建玩家 " + intarg + " 个消耗时间：" + (System.currentTimeMillis() - start));

	}

}
