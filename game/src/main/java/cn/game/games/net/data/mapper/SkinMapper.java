package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.Skin;

public interface SkinMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("skin") Integer skin);

	/**
	 * @mbg.generated
	 */
	int insert(Skin row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Skin row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Skin row);

	/**
	 * @mbg.generated
	 */
	Skin selectByPrimaryKey(@Param("playerId") Long playerId, @Param("skin") Integer skin);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Skin row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Skin row);

	/**
	 * @mbg.generated
	 */
	List<Skin> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<Skin> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<Skin> records);

}
