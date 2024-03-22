package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.HomePage;
import java.util.List;

public interface HomePageMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long playerId);

	/**
	 * @mbg.generated
	 */
	int insert(HomePage row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(HomePage row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(HomePage row);

	/**
	 * @mbg.generated
	 */
	HomePage selectByPrimaryKey(Long playerId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(HomePage row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(HomePage row);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<HomePage> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<HomePage> records);
}