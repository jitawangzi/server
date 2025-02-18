package db;

import java.util.ArrayList;
import java.util.List;

import cn.game.core.base.ServerContext;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.cache.entity.Base;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.cache.entity.Variable;
import cn.game.games.net.data.mapper.BaseMapper;
import cn.game.games.net.data.mapper.PlayerDataMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.module.quest.Quest;
import cn.game.games.util.DAO;
import cn.game.util.DateUtil;
import cn.game.util.JsonUtil;
import cn.game.util.ObjUtil;
import cn.game.util.RedisUtil;
import cn.game.util.Rnd;
import cn.game.util.SpringApolloLoader;
import cn.game.util.ZkHelper;

public class DbExcute {

	public static void main(String[] args) throws Exception {
		RedisUtil.getInstance().init();
		ZkHelper.init();
		ServerContext.getInstance().init();
		VxHolder.init();

		SpringApolloLoader springApolloLoader = new SpringApolloLoader();
		springApolloLoader.init();

		long start = System.currentTimeMillis();

//		testBatchInsert();
//		testQuestBatch() ; 
//		testPlayerBatch();
		insertPlayerBatch();
//		updatePlayerBatch();

		System.err.println("执行总耗时： " + (System.currentTimeMillis() - start) + " ms");
	}
	
	/** 
	 * 9w人后，1w人要1.5分钟了。和设置的var数量有关，也就是数量越大，越慢 ，暂时和数据库已经数据量没多大关系。 
	 */
	public static void insertPlayerBatch() {
		long id = 400530000;

		for (int a = 0; a < 1000000; a++) {
			List<PlayerData> list = new ArrayList<>();
			for (int i = 0; i < 50; i++) {
				PlayerData playerData = new PlayerData();
				playerData.setPlayerId(id++);
				playerData.setUid(id);
				playerData.setGender(true);
				playerData.setCreateDate(DateUtil.getStringDate());
				playerData.setName(id + "");
				playerData.setHead(33);
				playerData.setHeadFrame(22);
//				playerData.setRegion(AddressUtil.getCityInfo(client.getIp()));
				playerData.setLoginDate(DateUtil.getStringDate());
				playerData.setVipExpTotal(0);
				playerData.setVipLevel(1); // 好感度默认1级
				playerData.setRefreshDay(DateUtil.getDayCustom());

				ObjUtil.setDefaultValue(playerData);

				Player player = new Player(playerData);

				for (int j = 0; j < 300; j++) {
//					player.getVarModule().setVar(j, j);
				}

				playerData.setModules(JsonUtil.toJsonString(player.getModules()));
				list.add(playerData);
			}
			DAO.executeSync(PlayerDataMapper.class, MapperConstant.insertBatch, list);
		}

	}

	/** 
	 * 更新的时间和插入的时间差不多
	 */
	public static void updatePlayerBatch() {
		long id = 300700000;

		for (int a = 0; a < 200; a++) {
			List<PlayerData> list = new ArrayList<>();
			for (int i = 0; i < 50; i++) {
				PlayerData playerData = new PlayerData();
				playerData.setPlayerId(id++);
				playerData.setUid(id);
				playerData.setGender(true);
				playerData.setCreateDate(DateUtil.getStringDate());
				playerData.setName(id + "");
				playerData.setHead(33);
				playerData.setHeadFrame(22);
//				playerData.setRegion(AddressUtil.getCityInfo(client.getIp()));
				playerData.setLoginDate(DateUtil.getStringDate());
				playerData.setVipExpTotal(0);
				playerData.setVipLevel(1); // 好感度默认1级
				playerData.setRefreshDay(DateUtil.getDay());

				ObjUtil.setDefaultValue(playerData);

				Player player = new Player(playerData);

				for (int j = 1500; j < 1800; j++) {
//					player.getVarModule().setVar(j, j);
				}

				playerData.setModules(JsonUtil.toJsonString(player.getModules()));
				list.add(playerData);
//				DAO.executeSync(PlayerDataMapper.class, MapperConstant.updateByPrimaryKeyWithBLOBs, playerData);

			}
			DAO.executeSync(PlayerDataMapper.class, MapperConstant.updateBatch, list);
		}
	}

	public static void testPlayerBatch() {
		
		PlayerData player1 = (PlayerData) DAO.executeSync(PlayerDataMapper.class, MapperConstant.selectByPrimaryKey, 242110003L); 
		PlayerData player2 = (PlayerData) DAO.executeSync(PlayerDataMapper.class, MapperConstant.selectByPrimaryKey, 242110004L); 
		PlayerData player3 = (PlayerData) DAO.executeSync(PlayerDataMapper.class, MapperConstant.selectByPrimaryKey, 242110005L); 
		
		player1.setLevel(112);
		player2.setLevel(223);
		player3.setLevel(334);
		
		
		List<PlayerData> list = new ArrayList<>();
		list.add(player1) ; 
		list.add(player2) ; 
		list.add(player3) ; 
//		DAO.insertBatch(buff.getMapperClass(), list);
		DAO.execute(PlayerDataMapper.class, "updateBatch", list);
	}
	public static void testQuestBatch() {
			List<Quest> list = new ArrayList<>();
			Quest buff = new Quest();
			buff.setId(92230);
			buff.setPlayerId(22222L);
			buff.setState((byte) 5);
			buff.setEndTime(5555L);
//			buff.insert();
			list.add(buff);

			buff = new Quest();
			buff.setId(92231);
			buff.setPlayerId(22222L);
			buff.setState((byte) 10);
			buff.setEndTime(5555L);
			buff.setParams("hahaha");
//			buff.insert();

			list.add(buff);
//			DAO.insertBatch(buff.getMapperClass(), list);
			DAO.execute(buff.getMapperClass(), "updateBatch", list);
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
