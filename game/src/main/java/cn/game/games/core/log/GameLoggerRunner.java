package cn.game.games.core.log;

/**
 * @author PangHongFeng
 * @date 2023/4/18
 * 日志线程，目前用来处理比较耗费资源的快照打印
 */
public class GameLoggerRunner {

	/* *//**
			* 线程执行间隔毫秒
			*/
	/*
	private static final long INTERVAL_TIME = 1000;
	*//**
		* 每次线程执行期打印玩家数量（200万玩家300个节点，平均每个节点6666人，人均打印耗时0.2秒）
		*/
	/*
	private static final int LOG_COUNT = 10;//按每节点日活6000人计算，每10秒打印10个玩家，打印耗时2秒，供需120分钟左右完成
	
	private Set<String> serverNumSet = new HashSet<>();
	*//**
		* 最后一次打印荣誉榜快照日志时间戳毫秒
		*/
	/*
	private long lastRankLogTime = ServerConstants.getCurrentTimeMillis();
	*//**
		* 最后一次打印竞技场榜单快照日志时间戳毫秒
		*/
	/*
	private long lastArenaRankLogTime = ServerConstants.getCurrentTimeMillis();
	
	//上次每日登录玩家信息清除
	private long lastDailyLoginRemove = ServerConstants.getCurrentTimeMillis();
	
	*//**
		* 上次公会成员快照时间
		*/
	/*
	private long lastDailyLeagueLogTime = ServerConstants.getCurrentTimeMillis();
	
	private static GameLoggerRunner instance = new GameLoggerRunner();
	
	public static GameLoggerRunner getInstance() {
	 return instance;
	}
	
	public static boolean nowLogger = false;
	
	@Override
	public void runnerExecute() throws Exception {
	 long now = ServerConstants.getCurrentTimeMillis();
	
	 if (nowLogger || !DateTimeUtil.inSameType(DateTimeDurationType.day, now + DateTimeUtil.MillisOfMinute * 10,
	         lastRankLogTime)) {
	     SystemLogger.debug("GameLoggerRunner ==> RankLog start:" + ServerConstants.getCurrentTimeMillis());
	//            for (Integer areaNumber : GameAreaLogicRunner.getAllAreaNumbers()) {
	//                String serverNumString = String.valueOf(areaNumber);
	//                if (!serverNumSet.contains(serverNumString)) {
	//                    serverNumSet.add(serverNumString);
	//                    executeRankLog(areaNumber);
	//                }
	//            }
	     SystemLogger.debug("GameLoggerRunner ==> RankLog end:" + ServerConstants.getCurrentTimeMillis());
	//            if (serverNumSet.size() == GameAreaLogicRunner.getAllAreaNumbers().size()) {
	//                serverNumSet.clear();
	//                lastRankLogTime = now + DateTimeUtil.MillisOfMinute * 10;
	//            }
	 }
	
	 if (nowLogger || !DateTimeUtil.inSameType(DateTimeDurationType.day, now + DateTimeUtil.MillisOfMinute * 10,
	         lastDailyLoginRemove)) {
	     SystemLogger.debug("GameLoggerRunner ==> PropertysnapLog start:" + ServerConstants.getCurrentTimeMillis());
	//            for (Integer areaNumber : GameAreaLogicRunner.getAllAreaNumbers()) {
	//                long oldLoginTime = lastDailyLoginRemove;
	//                lastDailyLoginRemove = now + DateTimeUtil.MillisOfMinute * 10;
	//                executePropertysnapLog(areaNumber, oldLoginTime);
	//            }
	     SystemLogger.debug("GameLoggerRunner ==> PropertysnapLog end:" + ServerConstants.getCurrentTimeMillis());
	 }
	
	 if (nowLogger || !DateTimeUtil.inSameType(DateTimeDurationType.day, now + DateTimeUtil.MillisOfMinute * 10,
	         lastArenaRankLogTime)) {
	     lastArenaRankLogTime = now + DateTimeUtil.MillisOfMinute * 10;
	     SystemLogger.debug("GameLoggerRunner ==> ArenaRankLog start:" + ServerConstants.getCurrentTimeMillis());
	//            for (Integer areaNumber : GameAreaLogicRunner.getAllAreaNumbers()) {
	//                executeArenaRankLog(areaNumber,nowLogger);
	//            }
	     SystemLogger.debug("GameLoggerRunner ==> ArenaRankLog end:" + ServerConstants.getCurrentTimeMillis());
	 }
	
	 if (nowLogger || !DateTimeUtil.inSameType(DateTimeDurationType.day, now + DateTimeUtil.MillisOfMinute * 10,
	         lastDailyLeagueLogTime)) {
	     lastDailyLeagueLogTime = now + DateTimeUtil.MillisOfMinute * 10;
	     SystemLogger.debug("GameLoggerRunner ==> LeagueMemberLog start:" + ServerConstants.getCurrentTimeMillis());
	//            for (Integer areaNumber : GameAreaLogicRunner.getAllAreaNumbers()) {
	//                executeLeagueMemberLog(areaNumber,nowLogger);
	//            }
	     SystemLogger.debug("GameLoggerRunner ==> LeagueMemberLog end:" + ServerConstants.getCurrentTimeMillis());
	 }
	 nowLogger = false;
	}
	
	@Override
	public long getRunnerInterval() {
	 return INTERVAL_TIME;
	}
	
	private void executeRankLog(int serverNum) {
	}
	
	*//**
		* 玩家属性快照
		*
		* @param serverNum
		*//*
			private void executePropertysnapLog(int serverNum, long lastDailyLoginRemove) {
			}
			*/

}
