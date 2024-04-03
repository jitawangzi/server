package cn.game.games.cache.op.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import cn.game.games.cache.entity.Draw;
import cn.game.games.cache.op.face.IDrawOp;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.DrawMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.DrawGroupConfig;
import cn.game.protocol.generated.config.DrawNoviceConfig;
import cn.game.protocol.generated.config.DrawRountineConfig;
import cn.game.protocol.generated.config.DrawUpConfig;
import cn.game.protocol.generated.config.OldGlobalConst;
import cn.game.protocol.generated.config.RoleConfig;
import cn.game.protocol.generated.manager.DrawRountineManager;
import cn.game.protocol.generated.manager.DrawUpManager;
import cn.game.protocol.generated.manager.RoleManager;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.util.Rnd;

/** 抽卡逻辑 */
public class DrawOp extends BasePlayerModule implements IDrawOp {
	/** 普通卡池id */
	public static final int COMMON_ID = 1;
	/** UP卡池id */
	public static final int UP_ID = 2;
	/** 新手卡池id */
	public static final int NOVICE_ID = 3;

	/** 各卡池通用权重和 */
	private static final int WEIGHT_SUM = 10000;

	/** 新手卡池累计次数 */
	private int noviceTimes;

	/** 新手卡池当前奖励 */
	private List<Integer> noviceRewards;

	/** 普通卡池累计次数 */
	private int commonTimes;

	/** UP卡池累计次数 */
	private int upTimes;

	/** 数据库是否有数据 */
	private boolean isDb;

	/** 普通卡池稀有度保底 */
	private static final int COMMON_QUALITY = 0;

	/** UP卡池稀有度保底 */
	private static final int UP_QUALITY = 1;

	/** 普通卡池次数保底 */
	private static final int COMMON_TIMES = 2;

	/**
	 * 保底角色组缓存 [0]普通卡池稀有度角色组 [1]UP卡池稀有度角色组 [2]普通卡池次数角色组
	 */
	private static final List<DrawRountineConfig>[] cache = new ArrayList[3];

	/** 对应角色组上次缓存的时间戳 */
	private static final long[] cacheTime = new long[3];

	/** 一分钟的毫秒数 */
	private static final long MSEC_OF_ONE_MINUTE = 1000 * 60;

	@Override
	public void init() {
		
	}

	public void initLoadData(Draw draw) {
		if (draw != null) {
			noviceTimes = draw.getNoviceTimes();
			commonTimes = draw.getCommonTimes();
			upTimes = draw.getUpTimes();
			isDb = true;
		}
	}

	@Override
	public int getNoviceTimes() {

		return noviceTimes;
	}

	@Override
	public int getCommonTimes() {

		return commonTimes;
	}

	@Override
	public int getUpTimes() {

		return upTimes;
	}

	/**
	 * 新手卡池抽卡
	 */
	public List<Integer> noviceDraw(Collection<DrawNoviceConfig> drawNoviceConfigs) {
		noviceTimes++;
		if (noviceTimes >= OldGlobalConst.drawNoviceLimit) {
			noviceTimes = -1;
		}
		return noviceRandom(drawNoviceConfigs);
	}

	private List<Integer> noviceRandom(Collection<DrawNoviceConfig> drawNoviceConfigs) {
		int randomNum = getRandomNum();
		int weightSum = 0;

		for (DrawNoviceConfig conf : drawNoviceConfigs) {
			weightSum += conf.getWeight();
			if (weightSum >= randomNum) {
				this.noviceRewards = conf.getAwardList();
				return this.noviceRewards;
			}
		}
		return null;
	}

	private static int getRandomNum() {
		return Rnd.get(1, WEIGHT_SUM);
	}

	/**
	 * 新手卡池上次的抽奖结果
	 * 
	 * @return
	 */
	public List<Integer> getNoviceRewards() {
		return noviceRewards;
	}

	/**
	 * 清除新手卡池奖励
	 */
	public void clearNoviceRewards() {
		noviceTimes = -1;
		noviceRewards = null;
	}

	@Override
	public boolean lostNoviceChance() {
		return noviceTimes == -1;
	}

	/**
	 * 更新数据库
	 */
	public void updateDb() {
		Draw draw = Draw.valueOf(playerId, noviceTimes, commonTimes, upTimes);
		if (isDb) {
			update(draw);
		} else {
			insert(draw);
		}
		isDb = true;
	}

	private void insert(Draw draw) {
		DAO.execute(DrawMapper.class, MapperConstant.insert, draw);
	}

	private void update(Draw draw) {
		DAO.execute(DrawMapper.class, MapperConstant.updateByPrimaryKey,
				draw);
	}

//	--------------------------------以下普通卡池单抽----------------------------------

	/**
	 * 普通卡池单抽
	 * 
	 * @param commonDrawGroupConfig
	 * @return
	 */
	public List<Integer> commonOnce(DrawGroupConfig commonDrawGroupConfig) {
		this.commonTimes++;
		checkCommonTimesCache(commonDrawGroupConfig);
		// 卡池角色组
		Collection<DrawRountineConfig> drawRountineConfigList = DrawRountineManager.getInstance().list();

		// 次数上限
		int timesLimit = commonDrawGroupConfig.getAccumulate().get(0).getValue();
		List<Integer> reward = null;

		// 未到保底次数
		if (this.commonTimes < timesLimit) {
			reward = drawOnceFromCommonPool(drawRountineConfigList, commonDrawGroupConfig);
			return reward;
		}
		// 到达保底次数,保底角色组随机一个
		reward = drawFromCache(COMMON_TIMES);
		commonTimes = 0;
		return reward;
	}

	/**
	 * 检查普通卡池 次数保底缓存
	 * 
	 * @param commonDrawGroupConfig
	 */
	private void checkCommonTimesCache(DrawGroupConfig commonDrawGroupConfig) {
		boolean timeRight = rightTime(COMMON_TIMES);
		// 有正确缓存,返回
		if (cache[COMMON_TIMES] != null && timeRight) {
			return;
		}
		// 保底稀有度
		int quality = commonDrawGroupConfig.getAccumulate().get(0).getKey();
		// 卡池角色组
		Collection<DrawRountineConfig> drawRountineConfigList = DrawRountineManager.getInstance().list();
		// 缓存次数保底角色组
		checkCache(drawRountineConfigList, quality, COMMON_TIMES);
	}

	private boolean rightTime(int index) {
		return System.currentTimeMillis() - cacheTime[index] < MSEC_OF_ONE_MINUTE;
	}

	/**
	 * 缓存中随机一个角色
	 * 
	 * @param index
	 * @return
	 */
	private List<Integer> drawFromCache(int index) {
		List<Integer> res = new ArrayList<>();
		List<DrawRountineConfig> roleConfs = cache[index];

		int weightSum = 0;
		for (DrawRountineConfig conf : roleConfs) {
			weightSum += conf.getWeight();
		}

		int random = Rnd.get(1, weightSum);
		int sum = 0;
		for (DrawRountineConfig conf : roleConfs) {
			sum += conf.getWeight();
			if (sum >= random) {
				res.add(conf.getRoleId());
				break;
			}
		}
		return res;
	}

	/**
	 * 缓存某稀有度的角色组
	 * 
	 * @param roleConfs
	 * @param targetQuality
	 * @param index
	 */
	private void checkCache(Collection<DrawRountineConfig> roleConfs, int targetQuality, int index) {
		List<DrawRountineConfig> cacheRoles = new ArrayList<>();
		
		for (DrawRountineConfig conf : roleConfs) {
			int roleId = conf.getRoleId();
			RoleConfig roleConfig = RoleManager.getInstance().getRoleConfig(roleId);
			
			if (roleConfig.getQuality() == targetQuality) {
				cacheRoles.add(conf);
			}
		}
		cache[index] = cacheRoles;
		cacheTime[index] = System.currentTimeMillis();
	}

	/**
	 * 未到累计次数的单抽
	 * 
	 * @param drawRountineConfigs
	 * @param commonDrawGroupConfig
	 * @return
	 */
	private List<Integer> drawOnceFromCommonPool(Collection<DrawRountineConfig> drawRountineConfigs,
			DrawGroupConfig commonDrawGroupConfig) {
		List<DrawRountineConfig> pool = new ArrayList<>(drawRountineConfigs);

		List<Integer> oneRole = drawByTimesSlow(pool, 1);
		if (oneRole.isEmpty()) {
			return oneRole;
		}

		// 抽中保底角色,次数清零
		int ensureQuality = commonDrawGroupConfig.getAccumulate().get(0).getKey();
		boolean ensure = checkQuality(oneRole.get(0), ensureQuality);
		if (ensure) {
			this.commonTimes = 0;
		}

		return oneRole;
	}

	/**
	 * 是否是保底角色
	 * 
	 * @param roleId
	 * @param quality
	 */
	private boolean checkQuality(int roleId, int quality) {
		RoleConfig roleConfig = RoleManager.getInstance().getRoleConfig(roleId);
		if (roleConfig != null && roleConfig.getQuality() == quality) {
			return true;
		}

		return false;
	}

//	--------------------------------以下up卡池单抽----------------------------------

	/**
	 * UP卡池单抽
	 * 
	 * @param upDrawGroupConfig
	 * @return
	 */
	public List<Integer> upOnce(DrawGroupConfig upDrawGroupConfig) {
		this.upTimes++;
		// 保底角色是固定的一个角色,不用从缓存中抽取
		List<Integer> reward = new ArrayList<>();
		// 保底角色
		int ensureRole = upDrawGroupConfig.getFloorsRole().get(0).getKey();
		int limitTimes = upDrawGroupConfig.getFloorsRole().get(0).getValue();

		// 当前未达到保底次数
		if (upTimes < limitTimes) {
			reward = drawOnceFromUpPool();
			if (!reward.isEmpty() && reward.get(0) == ensureRole) {
				upTimes = 0;
			}
			return reward;
		}

		upTimes = 0;
		// 达到保底次数，奖励一个固定的角色
		reward.add(ensureRole);
		return reward;
	}

	private List<Integer> drawOnceFromUpPool() {
		Collection<DrawUpConfig> list = DrawUpManager.getInstance().list();
		List<DrawRountineConfig> pool = new ArrayList<>(list);
		List<Integer> oneRole = drawByTimesSlow(pool, 1);

		return oneRole;
	}

//	--------------------------------普通卡池十连抽----------------------------------
	/**
	 * 普通卡池十连抽
	 * 
	 * @param drawGroupConfig
	 * @return 十个角色id
	 */
	public List<Integer> commonTen(DrawGroupConfig drawGroupConfig) {
		// 角色组
		Collection<DrawRountineConfig> list = DrawRountineManager.getInstance().list();
		// Collection->List
		List<DrawRountineConfig> pool = new ArrayList<>(list);

		// 检查稀有度角色组缓存
		checkCommonQualityCache(drawGroupConfig, list);

		// 检查保底角色组缓存COMMON_times(COMMON特有 UP没有)
		checkCommonTimesCache(drawGroupConfig);

		// 次数保底上限
		int timesLimit = drawGroupConfig.getAccumulate().get(0).getValue();

		// 卡池抽9次
		List<Integer> rewards = drawByTimesSlow(pool, 9);

		// 十连必中角色,插入后半段
		int mustId = drawFromCache(COMMON_QUALITY).get(0);
		int insertIndex = Rnd.get(rewards.size() / 2, rewards.size() - 1);
		rewards.add(insertIndex, mustId);

		for (int i = 0; i < rewards.size(); i++) {
			commonTimes++;
			// 十连抽过程中,触发次数保底
			if (commonTimes >= timesLimit) {
				commonTimes = 0;
				// 随机一个保底角色
				int roleId = drawFromCache(COMMON_TIMES).get(0);
				// 十连抽保底角色和次数保底角色位置重叠
				if (i == insertIndex && i > 0) {
					// 十连抽保底角色前移一位
					rewards.set(i - 1, mustId);
				}
				rewards.set(i, roleId);
				continue;
			}

			// 十连抽过程中抽到次数保底角色,次数清零
			int roleId = rewards.get(i);
			int ensureQuality = drawGroupConfig.getAccumulate().get(0).getKey();
			boolean isEnsure = checkQuality(roleId, ensureQuality);
			if (isEnsure) {
				this.commonTimes = 0;
			}
		}

		return rewards;
	}

	private void checkCommonQualityCache(DrawGroupConfig drawGroupConfig, Collection<DrawRountineConfig> list) {

		boolean timeRight = rightTime(COMMON_QUALITY);
		// 有正确缓存,返回
		if (cache[COMMON_QUALITY] != null && timeRight) {
			return;
		}
		// 保底稀有度
		int targetQuality = drawGroupConfig.getFloorsQuality();
		// 缓存角色组
		checkCache(list, targetQuality, COMMON_QUALITY);
	}

	/**
	 * 按次数慢抽
	 * 
	 * @param pool
	 * @param times
	 * @return
	 */
	@Override
	public List<Integer> drawByTimesSlow(List<DrawRountineConfig> pool, int times) {
		List<Integer> result = new ArrayList<>(times);
		for (int i = 0; i < times; i++) {
			int roleId = slowDraw(pool);
			if (roleId != -1) {
				result.add(roleId);
			}
		}
		return result;
	}

	/**
	 * 从某卡池中按权重,快抽
	 * 
	 * @param pool
	 * @return
	 */
	private List<Integer> drawByTimesQuick(List<DrawRountineConfig> pool, int times) {
		List<Integer> result = new ArrayList<>(times);

		for (int i = 0; i < times; i++) {
			int roleId = binarySearchDraw(pool);
			result.add(roleId);
		}

		return result;


	}

	private int slowDraw(List<DrawRountineConfig> pool) {
		int random = getRandomNum();
		int weightSum = 0;
		for (DrawRountineConfig conf : pool) {
			weightSum += conf.getWeight();
			if (weightSum >= random) {
				return conf.getRoleId();
			}
		}
		return -1;
	}

	/**
	 * 二分查找
	 * 
	 * @param pool
	 * @return
	 */
	private int binarySearchDraw(List<DrawRountineConfig> pool) {
//		int weightSum = pool.get(pool.size() - 1).getPoint();
//		int key = Rnd.get(1, weightSum);
//
//		int left = 0;
//		int right = pool.size();
//
//		while (left <= right) {
//			int mid = (left + right) / 2;
//			int curWeight = pool.get(mid).getPoint();
//
//			if (curWeight >= key) {
//
//				if (mid == 0 || pool.get(mid - 1).getPoint() < key) {
//					return pool.get(mid).getRoleId();
//				} else {
//					right = mid - 1;
//				}
//
//			} else {
//				left = mid + 1;
//			}
//
//		}
		return -1;

	}

//	--------------------------------以下UP卡池十连抽----------------------------------
	/**
	 * UP卡池十连抽
	 * 
	 * @param drawGroupConfig
	 * @return
	 */
	public List<Integer> upTen(DrawGroupConfig drawGroupConfig) {
		// 角色组
		Collection<DrawUpConfig> list = DrawUpManager.getInstance().list();
		// Collection->List
		List<DrawRountineConfig> transList = new ArrayList<>(list);
		// 检查稀有度角色组缓存
		checkUpQualityCache(drawGroupConfig, transList);

		// 次数保底上限
		int timesLimit = drawGroupConfig.getFloorsRole().get(0).getValue();

		// 卡池抽十次
		List<Integer> reward = drawByTimesSlow(transList, 9);

		// 十连必中角色,插入后半段
		int mustId = drawFromCache(UP_QUALITY).get(0);
		int randomIndex = Rnd.get(reward.size() / 2, reward.size() - 1);
		reward.add(randomIndex, mustId);
		// System.out.println("十连抽必中角色：" + mustId + "插入第" + randomIndex + "位");
		// 十连抽中触发 次数保底
		for (int i = 0; i < reward.size(); i++) {
			upTimes++;
			// 十连抽中间触发 次数保底
			if (upTimes >= timesLimit) {
				upTimes = 0;
//				替换成次数保底角色
				int floorsRoleId = drawGroupConfig.getFloorsRole().get(0).getKey();
				if (i == randomIndex && i > 0) {
					reward.set(i - 1, mustId);
				}
				reward.set(i, floorsRoleId);
				continue;
			}
			// 次数保底角色
			int timesEnsureRole = drawGroupConfig.getFloorsRole().get(0).getKey();
			if (reward.get(i) == timesEnsureRole) {
				upTimes = 0;
			}

		}
		return reward;
	}

	/**
	 * 检查并缓存UP卡池十连抽保底(quality)角色
	 * 
	 * @param drawGroupConfig
	 * @param transList
	 */
	private void checkUpQualityCache(DrawGroupConfig drawGroupConfig, List<DrawRountineConfig> transList) {
		boolean timeRight = rightTime(UP_QUALITY);
		// 有正确缓存,返回
		if (cache[UP_QUALITY] != null && timeRight) {
			return;
		}
		// 保底稀有度
		int quality = drawGroupConfig.getFloorsQuality();

		// 缓存次数保底角色组
		checkCache(transList, quality, UP_QUALITY);
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleEvent(GameEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public Class<?>[] defaultDbMapperClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void initFromDb(ListIterator<?> iterator) {
		// TODO Auto-generated method stub

	}
	@Override
	public void initFromDbAfter() {

	};
	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}

}
