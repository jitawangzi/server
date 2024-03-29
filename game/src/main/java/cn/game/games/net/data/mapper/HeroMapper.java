package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.Hero;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface HeroMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int insert(Hero row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Hero row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Hero row);

	/**
	 * @mbg.generated
	 */
	Hero selectByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Hero row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Hero row);

	/**
	 * @mbg.generated
	 */
	List<Hero> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<Hero> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<Hero> records);

	/**
	 * @mbg.generated
	 */
	int batchUpdate(@Param("recordList") List<Hero> recordList);
}