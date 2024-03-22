package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.Variable;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface VariableMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("type") Integer type);

	/**
	 * @mbg.generated
	 */
	int insert(Variable row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Variable row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Variable row);

	/**
	 * @mbg.generated
	 */
	Variable selectByPrimaryKey(@Param("playerId") Long playerId, @Param("type") Integer type);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Variable row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Variable row);

	/**
	 * @mbg.generated
	 */
	List<Variable> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<Variable> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<Variable> records);
}