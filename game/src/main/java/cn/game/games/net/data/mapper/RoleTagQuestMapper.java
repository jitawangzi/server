package cn.game.games.net.data.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import cn.game.games.cache.entity.RoleTagQuest;

public interface RoleTagQuestMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("roleDictId") Integer roleDictId, @Param("tagId") Integer tagId);

	/**
	 * @mbg.generated
	 */
	int insert(RoleTagQuest row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(RoleTagQuest row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(RoleTagQuest row);

	/**
	 * @mbg.generated
	 */
	RoleTagQuest selectByPrimaryKey(@Param("playerId") Long playerId, @Param("roleDictId") Integer roleDictId, @Param("tagId") Integer tagId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(RoleTagQuest row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeyWithBLOBs(RoleTagQuest row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(RoleTagQuest row);

	/**
	 * @mbg.generated
	 */
	List<RoleTagQuest> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<RoleTagQuest> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<RoleTagQuest> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<RoleTagQuest> recordList);
}