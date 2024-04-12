package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.Users;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UsersMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Integer id);

	/**
	 * @mbg.generated
	 */
	int insert(Users row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Users row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Users row);

	/**
	 * @mbg.generated
	 */
	Users selectByPrimaryKey(Integer id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Users row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Users row);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<Users> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<Users> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<Users> recordList);
}