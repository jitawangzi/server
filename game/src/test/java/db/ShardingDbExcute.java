package db;

import java.util.ArrayList;
import java.util.List;

import cn.game.core.base.ServerContext;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.cache.entity.Base;
import cn.game.games.cache.entity.Variable;
import cn.game.games.net.data.mapper.BaseMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.module.quest.Quest;
import cn.game.games.util.DAO;
import cn.game.util.Rnd;
import cn.game.util.ServerType;
import cn.game.util.SpringApolloLoader;

/**    
 * shardingsphere对于;分隔的多sql执行不支持。
 * 2024年3月28日 下午4:40:04
 * @author SYQ
 */
public class ShardingDbExcute {

	public static void main(String[] args) throws Exception {
		ServerContext.getInstance().init("SYQ", ServerType.Data);
		VxHolder.init();

		SpringApolloLoader springApolloLoader = new SpringApolloLoader();
		springApolloLoader.init();
//		testBatchInsert();

		List<Quest> list = new ArrayList<>();
		Quest item = new Quest();
		item.setId(92230);
		item.setPlayerId(22222L);
		item.setState((byte) 0);
		item.setEndTime(System.currentTimeMillis());
		item.setStartTime(System.currentTimeMillis());
//		buff.insert();
		list.add(item);

		item = new Quest();
		item.setId(92231);
		item.setPlayerId(22222L);
		item.setState((byte) 6);
		item.setEndTime(System.currentTimeMillis());
//		buff.insert();

		list.add(item);
		item = new Quest();
		item.setId(92232);
		item.setPlayerId(22222L);
		item.setState((byte) 8);
		item.setEndTime(2223333L);
//		buff.insert();

		list.add(item);
//		DAO.insertBatch(item.getMapperClass(), list);
		DAO.deleteBatch(item.getMapperClass(), list);
//		DAO.execute(item.getMapperClass(), "updateBatch", list);

	}

	public static void testBatchInsert() {
//		List<Buff> list = new ArrayList<>();
//		
//		for (int i = 0; i < 100000; i++) {
//
//			Buff buff = new Buff();
//			buff.setBuffId(1);
//			buff.setId(Rnd.nextLong());
//			buff.setPlayerId(10000L);
//			buff.setTarget(2222L);
//			buff.setUseNum(3);
//			list.add(buff);
//
//			buff = new Buff();
//			buff.setBuffId(2);
//			buff.setId(Rnd.nextLong());
//			buff.setPlayerId(10000L);
//			buff.setLevel(1);
//			buff.setTarget(2222L);
//			buff.setUseNum(3);
//			list.add(buff);
//		}
//
//		DAO.invoke(BuffMapper.class, "batchInsert", list);

	}

	public static void test() throws Exception {
		
		for (int i = 0; i < 100; i++) {
			
			Variable variable = new Variable();
			variable.setPlayerId(1L);
			variable.setType(1);
			variable.setValue(i);
			variable.insert();
		}
	}

	public static void testdb1() {

		long start = System.currentTimeMillis();
		int max = 10000;
		int cur = max;

		while (cur-- > 0) {
			int id = Rnd.get(1, max);
			Base row = new Base();
			row.setIntField1(id);
			row.setIntField10(Rnd.get(1, 10000000));
			DAO.invoke(BaseMapper.class, MapperConstant.updateByPrimaryKey, row);
		}
		System.out.println(max + " 次可选更新数据库操作耗时: " + (System.currentTimeMillis() - start));
	}

	public static void testdb2() {

		long start = System.currentTimeMillis();
		int max = 10000;
		int cur = max;

		while (cur-- > 0) {
			int id = Rnd.get(1, max);
			Base row = new Base();
			row.setIntField1(id);

			row.setIntField2(id * 5 + Rnd.get(1, 1000000));
			row.setIntField3(id * 5 + Rnd.get(1, 1000000));
			row.setIntField4(id * 5 + Rnd.get(1, 1000000));
			row.setIntField5(id * 5 + Rnd.get(1, 1000000));
			row.setIntField6(id * 5 + Rnd.get(1, 1000000));
			row.setIntField7(id * 5 + Rnd.get(1, 1000000));
			row.setIntField8(id * 5 + Rnd.get(1, 1000000));
			row.setIntField9(id * 5 + Rnd.get(1, 1000000));
			row.setIntField10(id * 5 + Rnd.get(1, 1000000));
			row.setIntField11(id * 5 + Rnd.get(1, 1000000));
			row.setIntField12(id * 5 + Rnd.get(1, 1000000));
			row.setIntField13(id * 5 + Rnd.get(1, 1000000));
			row.setIntField14(id * 5 + Rnd.get(1, 1000000));
			row.setIntField15(id * 5 + Rnd.get(1, 1000000));
			row.setIntField16(id * 5 + Rnd.get(1, 1000000));
			row.setIntField17(id * 5 + Rnd.get(1, 1000000));
			row.setIntField18(id * 5 + Rnd.get(1, 1000000));
			row.setIntField19(id * 5 + Rnd.get(1, 1000000));
			row.setIntField20(id * 5 + Rnd.get(1, 1000000));
			row.setIntField21(id * 5 + Rnd.get(1, 1000000));
			row.setIntField22(id * 5 + Rnd.get(1, 1000000));
			row.setIntField23(id * 5 + Rnd.get(1, 1000000));
			row.setIntField24(id * 5 + Rnd.get(1, 1000000));
			row.setIntField25(id * 5 + Rnd.get(1, 1000000));
			row.setIntField26(id * 5 + Rnd.get(1, 1000000));
			row.setIntField27(id * 5 + Rnd.get(1, 1000000));
			row.setIntField28(id * 5 + Rnd.get(1, 1000000));
			row.setIntField29(id * 5 + Rnd.get(1, 1000000));
			row.setIntField30(id * 5 + Rnd.get(1, 1000000));

			row.setStringField1(id + Rnd.get(1, 100000) + "ssssd3t");
			row.setStringField2(id + Rnd.get(1, 100000) + Rnd.get(1, 100000) + "ssssd3t");
			row.setStringField3(id + Rnd.get(1, 100000) + "ssssd3t");
			row.setStringField4(id + Rnd.get(1, 100000) + "ssssd3t");
			row.setStringField5(id + Rnd.get(1, 100000) + "ssssd3t");
			row.setStringField6(id + Rnd.get(1, 100000) + "ssssd3t");
			row.setStringField7(id + Rnd.get(1, 100000) + "ssssd3t");
			row.setStringField8(id + Rnd.get(1, 100000) + "ssssd3t");
			row.setStringField9(id + Rnd.get(1, 100000) + "ssssd3t");
			row.setStringField10(id + Rnd.get(1, 100000) + "ssssd3t");
			row.setStringField11(id + Rnd.get(1, 100000) + "ssssd3t");
			row.setStringField12(id + Rnd.get(1, 100000) + "ssssd3t");
			row.setStringField13(id + Rnd.get(1, 100000) + "ssssd3t");
			row.setStringField14(id + Rnd.get(1, 100000) + "ssssd3t");
			row.setStringField15(id + Rnd.get(1, 100000) + "ssssd3t");
			row.setStringField16(id + Rnd.get(1, 100000) + "ssssd3t");
			row.setStringField17(id + Rnd.get(1, 100000) + "ssssd3t");
			row.setStringField18(id + Rnd.get(1, 100000) + "ssssd3t");
			row.setStringField19(id + Rnd.get(1, 100000) + "ssssd3t");
			row.setStringField20(id + Rnd.get(1, 100000) + "ssssd3t");

			DAO.invoke(BaseMapper.class, MapperConstant.updateByPrimaryKey, row);
		}
		System.out.println(max + " 次全部更新数据库操作耗时: " + (System.currentTimeMillis() - start));
	}


	public static void initdb() {

		for (int i = 1; i < 10000; i++) {

			Base row = new Base();
			row.setIntField1(i);
			row.setIntField2(i * 3 + Rnd.get(1, 1000000));
			row.setIntField3(i * 3 + Rnd.get(1, 1000000));
			row.setIntField4(i * 3 + Rnd.get(1, 1000000));
			row.setIntField5(i * 3 + Rnd.get(1, 1000000));
			row.setIntField6(i * 3 + Rnd.get(1, 1000000));
			row.setIntField7(i * 3 + Rnd.get(1, 1000000));
			row.setIntField8(i * 3 + Rnd.get(1, 1000000));
			row.setIntField9(i * 3 + Rnd.get(1, 1000000));
			row.setIntField10(i * 3 + Rnd.get(1, 1000000));
			row.setIntField11(i * 3 + Rnd.get(1, 1000000));
			row.setIntField12(i * 3 + Rnd.get(1, 1000000));
			row.setIntField13(i * 3 + Rnd.get(1, 1000000));
			row.setIntField14(i * 3 + Rnd.get(1, 1000000));
			row.setIntField15(i * 3 + Rnd.get(1, 1000000));
			row.setIntField16(i * 3 + Rnd.get(1, 1000000));
			row.setIntField17(i * 3 + Rnd.get(1, 1000000));
			row.setIntField18(i * 3 + Rnd.get(1, 1000000));
			row.setIntField19(i * 3 + Rnd.get(1, 1000000));
			row.setIntField20(i * 3 + Rnd.get(1, 1000000));
			row.setIntField21(i * 3 + Rnd.get(1, 1000000));
			row.setIntField22(i * 3 + Rnd.get(1, 1000000));
			row.setIntField23(i * 3 + Rnd.get(1, 1000000));
			row.setIntField24(i * 3 + Rnd.get(1, 1000000));
			row.setIntField25(i * 3 + Rnd.get(1, 1000000));
			row.setIntField26(i * 3 + Rnd.get(1, 1000000));
			row.setIntField27(i * 3 + Rnd.get(1, 1000000));
			row.setIntField28(i * 3 + Rnd.get(1, 1000000));
			row.setIntField29(i * 3 + Rnd.get(1, 1000000));
			row.setIntField30(i * 3 + Rnd.get(1, 1000000));

			row.setStringField1(i * 3 + Rnd.get(1, 1000000) + "test");
			row.setStringField2(i * 3 + Rnd.get(1, 1000000) + "test");
			row.setStringField3(i * 3 + Rnd.get(1, 1000000) + "test");
			row.setStringField4(i * 3 + Rnd.get(1, 1000000) + "test");
			row.setStringField5(i * 3 + Rnd.get(1, 1000000) + "test");
			row.setStringField6(i * 3 + Rnd.get(1, 1000000) + "test");
			row.setStringField7(i * 3 + Rnd.get(1, 1000000) + "test");
			row.setStringField8(i * 3 + Rnd.get(1, 1000000) + "test");
			row.setStringField9(i * 3 + Rnd.get(1, 1000000) + "test");
			row.setStringField10(i * 3 + Rnd.get(1, 1000000) + "test");
			row.setStringField11(i * 3 + Rnd.get(1, 1000000) + "test");
			row.setStringField12(i * 3 + Rnd.get(1, 1000000) + "test");
			row.setStringField13(i * 3 + Rnd.get(1, 1000000) + "test");
			row.setStringField14(i * 3 + Rnd.get(1, 1000000) + "test");
			row.setStringField15(i * 3 + Rnd.get(1, 1000000) + "test");
			row.setStringField16(i * 3 + Rnd.get(1, 1000000) + "test");
			row.setStringField17(i * 3 + Rnd.get(1, 1000000) + "test");
			row.setStringField18(i * 3 + Rnd.get(1, 1000000) + "test");
			row.setStringField19(i * 3 + Rnd.get(1, 1000000) + "test");
			row.setStringField20(i * 3 + Rnd.get(1, 1000000) + "test");

			DAO.insert(row);
		}

	}
}
