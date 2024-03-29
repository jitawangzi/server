package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.PlayerExt;

public interface PlayerExtMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long playerId);

	/**
	 * @mbg.generated
	 */
	int insert(PlayerExt row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(PlayerExt row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(PlayerExt row);

	/**
	 * @mbg.generated
	 */
	PlayerExt selectByPrimaryKey(Long playerId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(PlayerExt row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeyWithBLOBs(PlayerExt row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(PlayerExt row);

	/**
	 * @mbg.generated
	 */
	List<PlayerExt> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<PlayerExt> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<PlayerExt> records);

	Long selectMaxId();
	
    /**
     * @mbg.generated
     */
    int batchUpdate(@Param("recordList") List<PlayerExt> recordList) ;
}