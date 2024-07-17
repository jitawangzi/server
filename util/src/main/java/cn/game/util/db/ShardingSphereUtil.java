package cn.game.util.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;

import cn.game.util.ByteHelp;

/**    
 *  ShardingSphere 使用帮助类
 * 2022年10月12日 下午12:18:26
 * @author SYQ
 */
public class ShardingSphereUtil {

	/** 默认分片规则的表字段，没有这个字段的表不创建分片规则 */
	private static final String SHARDING_KEY = "player_id";

	public static final String DBDRIVER = "com.mysql.cj.jdbc.Driver";

	// shardingsphere proxy，要生成分片规则的数据源 
	public static final String SHARDING_DATABASE = "game_sharding_db";
	public static final String SHARDING_DBURL = "jdbc:mysql://test:3340/" + SHARDING_DATABASE
			+ "?autoReconnect=true&zeroDateTimeBehavior=convertToNull&characterEncoding=UTF-8&serverTimezone=UTC&useSSL=false";
	public static final String SHARDING_DBUSER = "root";
	public static final String SHARDING_DBPASSWORD = "root";

	// 生成分片规则时需要的表所在数据源
	public static final String SRC_DATABASE = "game";
	public static final String SRC_DBURL = "jdbc:mysql://localhost:3306/" + SRC_DATABASE
			+ "?autoReconnect=true&zeroDateTimeBehavior=convertToNull&characterEncoding=UTF-8&serverTimezone=UTC&useSSL=false";
	public static final String SRC_DBUSER = "root";
	public static final String SRC_DBPASSWORD = "root";

	// 手动配置不生成分片规则的表。 一般是包含默认分片键，又不想分片的。 
	public static final List<String> tablesNotShardingWithPlayerId = Lists.newArrayList("t_account", "t_user_tag", "t_union",
			"t_group");
	/** 每个数据库里面分片表的数量 */
	public static final Map<String, Integer> tablesShardingCount = new HashMap<String, Integer>();

	static {
//		tablesShardingCount.put("t_item", 20);
//		tablesShardingCount.put("t_hero", 20);
//		tablesShardingCount.put("t_variable", 20);
//		tablesShardingCount.put("t_quest", 20);
//		tablesShardingCount.put("t_activity", 10);
//		tablesShardingCount.put("t_player_ids", 30);
		tablesShardingCount.put("t_mail", 5);
	}

	public static void main(String[] args) throws Exception {

		genShardingRuleFromSrcDb(false);
//		dropAllRules();
//		createStorageUnitSql();
	}

	/** 
	 * 分成分库分表规则，表名来自于另外的一个数据库,全量生成
	 * @param autoExecSql 是否自动执行创建分表规则的sql语句,推荐true,有时需要反复执行多次
	 * @throws Exception
	 */
	public static void genShardingRuleFromSrcDb(boolean autoExecSql) throws Exception {

		List<String> allTables = new ArrayList<>();
		List<String> tablesWithSharding = new ArrayList<>();
		Connection connSharding = getConnectionSharding();
		Connection connSrc = getConnectionSrc();
		Statement stmtSharding = connSharding.createStatement();
		Statement stmtSrc = connSrc.createStatement();

		String sql = "show tables";
		ResultSet resultSet = stmtSrc.executeQuery(sql);
		while (resultSet.next()) {
			String table = resultSet.getString("Tables_in_" + SRC_DATABASE);
			allTables.add(table);
		}
		// 过滤掉没有默认分片键的表
		List<String> notExistShardingKeyList = new ArrayList<>();
		for (String table : allTables) {

			String columnSql = "select COLUMN_NAME,COLUMN_TYPE,IS_NULLABLE,COLUMN_DEFAULT,"
	                + "COLUMN_COMMENT,EXTRA,CHARACTER_SET_NAME,COLLATION_NAME from information_schema.columns where TABLE_SCHEMA=" + "'" + SRC_DATABASE + "'"
	                + " and "
	                + "TABLE_NAME=" + "'" + table + "'" + " order by ORDINAL_POSITION asc" ; 
			resultSet = stmtSrc.executeQuery(columnSql);
			boolean hasDefaultShardingKey = false;
			while (resultSet.next()) {
				String column = resultSet.getString("COLUMN_NAME");
				if (column.equals(SHARDING_KEY)) {
					hasDefaultShardingKey = true;
					break;
				}
			}
			if (!hasDefaultShardingKey) {
//				System.err.println("table " + table + " not have sharding key " + SHARDING_KEY);
//				System.err.println("DROP SHARDING TABLE RULE " + table);
				notExistShardingKeyList.add(table);
			}
		}
		allTables.removeAll(notExistShardingKeyList);

		// 过滤掉已经存在规则的表
		sql = "SHOW SHARDING TABLE RULES FROM " + SHARDING_DATABASE;
		resultSet = stmtSharding.executeQuery(sql);
		while (resultSet.next()) {
			String table = resultSet.getString("table");
			tablesWithSharding.add(table);
		}
		allTables.removeAll(tablesWithSharding);
		// 过滤掉不分片的表
		allTables.removeAll(tablesNotShardingWithPlayerId);

		// 生成创建分片规则的语句并执行
		for (String table : allTables) {
			sql = genShardingRoleSqlMod(table);
//			sql = genShardingRoleSql(table);
			System.out.println(sql);
			if (autoExecSql) {
				try {
					stmtSharding.execute(sql);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		stmtSharding.close();
		connSharding.close(); //
		stmtSrc.close();
		connSrc.close(); //

	}

	private static String genShardingRoleSql(String table) {
		int dsCount = 3; //
		int idRange = 200000;
		StringBuilder rangebuilder = new StringBuilder();
		for (int i = 1; i < dsCount; i++) {
			rangebuilder.append(idRange * i).append(",");
		}
		String rangeString = rangebuilder.toString();

		StringBuilder dsbuilder = new StringBuilder();
		for (int i = 0; i < dsCount; i++) {
			dsbuilder.append("ds_" + i);
			if (i != dsCount - 1) {
				dsbuilder.append(",");
			}
		}
		String dsString = dsbuilder.toString();

		String string = "CREATE SHARDING TABLE RULE IF NOT EXISTS %s (STORAGE_UNITS(%s),SHARDING_COLUMN=%s, TYPE(NAME=\"%s\", PROPERTIES(\"sharding-ranges\"=\"%s\"))) ;";
		String sql = String.format(string, table, dsString, SHARDING_KEY, "BOUNDARY_RANGE", rangeString);
		return sql;
	}

	/** 
	 * 生成分片规则，MOD分片算法
	 * @param table
	 * @return
	 */
	private static String genShardingRoleSqlMod(String table) {
		int dsCount = 3; //
		int tableCount = dsCount;
		Integer integer = tablesShardingCount.get(table);
		if (integer != null && integer > 1) {
			tableCount *= integer;
		}
		String sql = String.format(
				"CREATE SHARDING TABLE RULE IF NOT EXISTS %s (STORAGE_UNITS(\"ds_${0..%d}\"),SHARDING_COLUMN=%s, TYPE(NAME=\"MOD\", PROPERTIES(\"sharding-count\"=\"%s\"))) ;",
				table, dsCount - 1, SHARDING_KEY, tableCount);
		return sql;
	}

	public static void dropAllRules() throws Exception {

		Connection connSharding = getConnectionSharding();
		Statement stmtSharding = connSharding.createStatement();
		String sql = "SHOW SHARDING TABLE RULES FROM " + SHARDING_DATABASE;
		ResultSet resultSet = stmtSharding.executeQuery(sql);
		List<String> tables = new ArrayList<>();
		while (resultSet.next()) {
			String table = resultSet.getString("table");
			tables.add(table);
//			System.out.println(sqlDrop);
		}
		for (String table : tables) {
			String sqlDrop = "DROP SHARDING TABLE RULE " + table + ";";
			stmtSharding.execute(sqlDrop);
		}
//		Thread.currentThread().join();
	}

	/** 
	 * 给存在的数据表，生成默认的分片规则，应该用不到了，最好先生成规则，在建表
	 * @throws Exception
	 */
	public static void genShardingRule() throws Exception {

		List<String> allTables = new ArrayList<>();
		List<String> tablesWithSharding = new ArrayList<>();
		Connection conn = getConnectionSharding();
		Statement stmt = conn.createStatement();
		String sql = "show tables";
		ResultSet resultSet = stmt.executeQuery(sql);
		while (resultSet.next()) {
			String table = resultSet.getString("Tables_in_" + SHARDING_DATABASE);
			allTables.add(table);
		}
		sql = "SHOW SHARDING TABLE RULES FROM " + SHARDING_DATABASE;
		resultSet = stmt.executeQuery(sql);
		while (resultSet.next()) {
			String table = resultSet.getString("table");
			tablesWithSharding.add(table);
//			System.err.println(table);
		}
		allTables.removeAll(tablesWithSharding);
		for (String table : allTables) {
			sql = genShardingRoleSql(table);
			System.out.println(sql);
			stmt.execute(sql);
		}

		stmt.close();
		conn.close(); //

	}

	public static void showShardingRule() throws Exception {

		List<String> allTables = new ArrayList<>();
		List<String> tablesWithSharding = new ArrayList<>();
		Connection conn = getConnectionSharding();
		Statement stmt = conn.createStatement();
		String sql = "show tables";
		ResultSet resultSet = stmt.executeQuery(sql);
		while (resultSet.next()) {
			String table = resultSet.getString("Tables_in_" + SHARDING_DATABASE);
			allTables.add(table);
		}
		sql = "SHOW SHARDING TABLE RULES FROM " + SHARDING_DATABASE;
		resultSet = stmt.executeQuery(sql);
		while (resultSet.next()) {
			String table = resultSet.getString("table");
			tablesWithSharding.add(table);
//			System.err.println(table);
		}
		allTables.removeAll(tablesWithSharding);
		for (String table : allTables) {
			sql = "CREATE SHARDING TABLE RULE " + table
					+ " (RESOURCES(ds_0,ds_1,ds_2,ds_3,ds_4),SHARDING_COLUMN=player_id, TYPE(NAME=\"MOD\", PROPERTIES(\"sharding-count\"=\"5\")))";
			System.out.println(sql);
//			stmt.execute(sql);
		}

		stmt.close();
		conn.close(); //

	}

	public static void testInsertBlob() throws Exception {
		
		byte[] oriArray = new byte[]{ 1,0,99,111,109,46,108,115,46,114,112,99,46,115,101,114,118,101,114,46,82,112,99,82,101,113,117,101,115,-12,49,48,46,48,46,52,46,49,-75,75,74,65,104,115,100,97,107,100,104,97,115,107,-28,115,101,116,68,97,116,-31,1,1,91,76,106,97,118,97,46,108,97,110,103,46,79,98,106,101,99,116,-69,6,9,-64,-39,-86,20,3,115,100,104,98,115,97,104,100,98,115,-31,1,2,99,111,109,46,108,115,46,114,112,99,46,115,101,114,118,101,114,46,82,112,99,82,101,115,112,111,110,115,-27,0,0,0,3,115,107,104,100,98,115,107,100,117,104,97,108,100,115,106,102,100,107,106,97,102,110,97,100,102,-22,10,66,61,-8,-5,108,25,0,0,3,2,0,3,0,0
		};

		String sql = "insert into data_record (`uuid`, `byteData`, `type`) values ('" + UUID.randomUUID().toString() + "','" + ByteHelp
				.strhex(oriArray) + "'," + 1 + ");";

		System.out.println(sql);
		Connection conn = getConnectionSharding();
		Statement stmt = conn.createStatement();
		stmt.execute(sql);
		conn.close();
	}

	public static void createStorageUnitSql() {
		List<String> list = new ArrayList<>();

		int min = 5;
		int max = 10;
		String addr = "127.0.0.1:3306";
		String ds = "ds_";
		String db = "game_ds_";
		String user = "root";
		String pass = "root";

		for (int i = min; i < max; i++) {

			String sql = "REGISTER STORAGE UNIT {0}{1} (\r\n"
					+ "    URL=\"jdbc:mysql://{2}/{3}{4}?serverTimezone=UTC&useSSL=false\",\r\n" + "    USER=\"{5}\",\r\n"
					+ "    PASSWORD=\"{6}\",\r\n" + "    PROPERTIES(\"maximumPoolSize\"=\"50\",\"idleTimeout\"=\"60000\")\r\n" + ");";
			sql = MessageFormat.format(sql, ds, i, addr, db, i, user, pass);
			list.add(sql);
			System.out.println(sql);
		}

	}

	public List<String> createDatabaseSql() {
		List<String> list = new ArrayList<>();
		for (int i = 5; i < 10; i++) {
			String sql = "create database game_ds_" + i + ";";
			list.add(sql);
		}
		return list;
	}
	public static void testInsertBlob2() throws Exception {

		byte[] oriArray = new byte[] { 1, 0, 99, 111, 109, 46, 108, 115, 46, 114, 112, 99, 46, 115, 101, 114, 118, 101, 114, 46, 82,
				112, 99, 82, 101, 113, 117, 101, 115, -12, 49, 48, 46, 48, 46, 52, 46, 49, -75, 75, 74, 65, 104, 115, 100, 97, 107, 100,
				104, 97, 115, 107, -28, 115, 101, 116, 68, 97, 116, -31, 1, 1, 91, 76, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 79,
				98, 106, 101, 99, 116, -69, 6, 9, -64, -39, -86, 20, 3, 115, 100, 104, 98, 115, 97, 104, 100, 98, 115, -31, 1, 2, 99,
				111, 109, 46, 108, 115, 46, 114, 112, 99, 46, 115, 101, 114, 118, 101, 114, 46, 82, 112, 99, 82, 101, 115, 112, 111,
				110, 115, -27, 0, 0, 0, 3, 115, 107, 104, 100, 98, 115, 107, 100, 117, 104, 97, 108, 100, 115, 106, 102, 100, 107, 106,
				97, 102, 110, 97, 100, 102, -22, 10, 66, 61, -8, -5, 108, 25, 0, 0, 3, 2, 0, 3, 0, 0 };
		String sql = "insert into data_record (`uuid`, `byteData`, `type`) values ('" + UUID.randomUUID().toString() + "','" + ByteHelp
				.strhex(oriArray) + "'," + 1 + ");";

		System.out.println(sql);
		Connection conn = getConnectionSharding();
		Statement stmt = conn.createStatement();
		stmt.execute(sql);
		conn.close();
	}

	private static Connection getConnectionSharding() throws Exception {

		Connection conn = null; //
		Class.forName(DBDRIVER); //
		conn = DriverManager.getConnection(SHARDING_DBURL, SHARDING_DBUSER, SHARDING_DBPASSWORD); //
		return conn;
	}
	private static Connection getConnectionSrc() throws Exception {

		Connection conn = null; //
		Class.forName(DBDRIVER); //
		conn = DriverManager.getConnection(SRC_DBURL, SRC_DBUSER, SRC_DBPASSWORD); //
		return conn;
	}

}
