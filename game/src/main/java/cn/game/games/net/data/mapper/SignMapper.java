package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.Sign;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SignMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long playerId);

	/**
	 * @mbg.generated
	 */
	int insert(Sign row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Sign row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Sign row);

	/**
	 * @mbg.generated
	 */
	Sign selectByPrimaryKey(Long playerId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Sign row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Sign row);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<Sign> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<Sign> records);

	/**
	 * @mbg.generated
	 */
	int batchUpdate(@Param("recordList") List<Sign> recordList);
}