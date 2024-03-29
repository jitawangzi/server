package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.Skill;

public interface SkillMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("id") Integer id);

	/**
	 * @mbg.generated
	 */
	int insert(Skill row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Skill row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Skill row);

	/**
	 * @mbg.generated
	 */
	Skill selectByPrimaryKey(@Param("playerId") Long playerId, @Param("id") Integer id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Skill row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Skill row);

	/**
	 * @mbg.generated
	 */
	List<Skill> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<Skill> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<Skill> records);

	/**
	 * @mbg.generated
	 */
	int batchUpdate(@Param("recordList") List<Skill> recordList);

}
