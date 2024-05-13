package db;

import java.util.ArrayList;
import java.util.List;

import cn.game.games.cache.entity.Buff;
import cn.game.games.net.data.mapper.BuffMapper;
import cn.game.games.util.DAO;
import cn.game.util.Rnd;
import cn.game.util.SpringApolloLoader;

public class DbExcute2 {

	public static void main(String[] args) throws Exception {
//		ServerContext.getInstance().init(ServerType.Game, "SYQ");

		SpringApolloLoader springApolloLoader = new SpringApolloLoader();
		springApolloLoader.init();

	}

	public static void testBatchInsert() {
		List<Buff> list = new ArrayList<>();

		for (int i = 0; i < 100000; i++) {

			Buff buff = new Buff();
			buff.setBuffId(1);
			buff.setId(Rnd.nextLong());
			buff.setPlayerId(10000L);
			buff.setTarget(2222L);
			buff.setUseNum(3);
			list.add(buff);

			buff = new Buff();
			buff.setBuffId(2);
			buff.setId(Rnd.nextLong());
			buff.setPlayerId(10000L);
			buff.setLevel(1);
			buff.setTarget(2222L);
			buff.setUseNum(3);
			list.add(buff);
		}

		DAO.invoke(BuffMapper.class, "batchInsert", list);

	}

	public static void testUpdateBatch() {
		List<Buff> list = new ArrayList<>();

		Buff buff = new Buff();
		buff.setBuffId(1);
		buff.setId(9216803894331940327L);
		buff.setPlayerId(20000L);
		buff.setTarget(2222L);
		buff.setUseNum(3);
		list.add(buff);

		buff = new Buff();
		buff.setBuffId(2);
		buff.setId(9216710231933264623L);
		buff.setPlayerId(20000L);
		buff.setLevel(1);
		buff.setTarget(2222L);
		buff.setUseNum(3);
		list.add(buff);

		DAO.invoke(BuffMapper.class, "batchUpate", list);

	}

}
