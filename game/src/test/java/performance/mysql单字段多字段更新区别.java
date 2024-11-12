package performance;

import cn.game.core.base.ServerContext;
import cn.game.games.cache.entity.Base;
import cn.game.games.net.data.mapper.BaseMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.util.DAO;
import cn.game.util.RedisUtil;
import cn.game.util.Rnd;
import cn.game.util.ServerType;
import cn.game.util.SpringApolloLoader;
import cn.game.util.ZkHelper;

/**    
 * 
 * 结论： 基本一样 。 测试的是基本类型字段的更新效率比较,
 * 
 * 没测出来有什么区别，都是50多秒执行完毕， 运行的时候磁盘占用都在80% - 90%左右，
 * 5，6m/s,看起来甚至是差不多的
 * 
 * msyql空闲124m
 * 从任务管理器里看mysql进程，两种方式占用内存都是在132m左右，磁盘有3m多的写入速度。 
 * 最终的执行时间都是在50多秒，看不出区别。 
 * 
 * 2024年2月23日 下午6:42:21
 * @author SYQ
 */
public class mysql单字段多字段更新区别 {

	public static void main(String[] args) throws Exception {
		RedisUtil.getInstance().init();
		ZkHelper.init();

		ServerContext.getInstance().init(new String[] { "SYQ" }, ServerType.Game);

		SpringApolloLoader springApolloLoader = new SpringApolloLoader();
		springApolloLoader.init();

		testdb1();
//		testdb2();
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
			DAO.invoke(BaseMapper.class, MapperConstant.updateByPrimaryKeySelective, row);
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
