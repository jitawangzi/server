package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.EventGame;

public interface EventGameMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("eventId") Integer eventId);

	/**
	 * @mbg.generated
	 */
	int insert(EventGame row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(EventGame row);

	/**
	 * @mbg.generated
	 */
	EventGame selectByPrimaryKey(@Param("playerId") Long playerId, @Param("eventId") Integer eventId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(EventGame row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(EventGame row);

	/**
	 * @mbg.generated
	 */
	List<EventGame> selectByPlayerId(@Param("playerId") Long playerId);

}
