package cn.game.games.cache.op.face;

import java.util.Collection;
import java.util.List;

import cn.game.games.cache.base.ICacheOp;
import cn.game.games.cache.entity.BattleEventType;
import cn.game.games.cache.entity.BattleLevel;
import cn.game.games.cache.entity.BattleRandomEvent;
import cn.game.games.cache.entity.Chapter;

public interface IChapterOp {

	// 初始数据,

	public void initLoadData(List<Chapter> chapters,
			List<BattleLevel> battleBattleLevels, List<BattleEventType> eventTypes,
			List<BattleRandomEvent> battleRandomEvents);

	/**
	 * @Description 更新章节
	 * @param chapter
	 */
	public void updateChapter(Chapter chapter);

	public void addChapter(Chapter chapter);

	/**
	 * @Description 更新关卡
	 * @param stage
	 */
	public void updateBattleLevel(BattleLevel stage);

	/**
	 * @Description 获取玩家所在最新的一章，如果没打过，能解锁的也算
	 * @return
	 */
	public int getNewChapter();

	/**
	 * @Description 某一关是否通关了
	 * @param levelId
	 * @return
	 */
	public boolean isBattleLevelPass(int levelId);

	/**
	 * @Description 探索幕是否通关
	 * @param id
	 * @return
	 */
	public boolean isExploreActPass(int id);
	/**
	 * @Description 探索章是否通关
	 * @param id
	 * @return
	 */
	public boolean isExploreChapterPass(int id);

	public boolean exploreActReward(int id);
	public boolean exploreChapterReward(int id);


	/**
	 * @Description 距离下次生成随机事件的事件
	 * @return -1,如果不需要产生随机事件
	 */
	public int nextBattleEventTime();

	/**
	 * @Description 产生随机事件
	 * @return
	 */
	public boolean createBattleEvent();

	/**
	 * @Description 检查随机事件，如果有过期的则删除
	 */
	public void checkBattleEvent();

	public void insertBattleEvent(BattleRandomEvent event);

	public void removeBattleEvent(long id);

	public boolean hasBattleEvent(long id);

	public BattleRandomEvent getBattleEvent(long id);

	/**
	 * @Description 某章节是否通关,指的是完成章节里的所有关卡，完成度到100%
	 * @param chapterId
	 * @return
	 */
	public boolean isChapterPass(int chapterId);

	public void setAttackingData(int lineupId, int type, int dungeonId, int id, long uid, long randomSeed);

	public int getAttackingId();

	public int getAttackingType();

	public int getAttackingDungeonId();
	/**
	 * @Description 某唯一id
	 * @return
	 */
	public long getAttackingUid();

	public int getLineupId();

	public boolean isBattleStarted();

	/**
	 * @Description 打完某一关卡
	 * @param levelId
	 * @param starList
	 * @return
	 */
	public boolean addBattleLevelPass(int levelId, List<Integer> starList);

	public boolean addBattleLevelPass(int levelId);

	public BattleLevel getBattleLevel(int levelId);

	public BattleLevel getBattleLevelAndInit(int levelId);

	public Chapter getChapter(int chapterId);

	public Collection<Chapter> listChapter();

	public int getAllStars();

	public int getStars(int zoneId);

	/**
	 * 进阶训练检查职业
	 * @param playerId
	 * @param profession
	 * @param lineupId
	 * @return
	 */
	public boolean checkProfession(long playerId, int profession, int lineupId, int type);

	public Collection<BattleLevel> listBattleLevels();

	public List<BattleRandomEvent> listBattleEvents();

}
