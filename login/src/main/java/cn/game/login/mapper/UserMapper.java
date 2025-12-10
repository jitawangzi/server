package cn.game.login.mapper;

import java.util.HashMap;

import org.apache.ibatis.annotations.Param;

import cn.game.login.cache.entity.User;
import java.util.List;

public interface UserMapper {
	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int insert(User row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(User row);

	/**
	 * @mbg.generated
	 */
	User selectByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(User row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(User row);

	/**
	 * @mbg.generated
	 */
	List<User> selectAll();

	/**
	 * @mbg.generated
	 */
	List<User> getBatchOffset(@Param("offset") int offset, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	List<User> getBatchCursor(@Param("lastId") Long lastId, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	User selectByNameChannel(@Param("username") String username, @Param("channelLabel") String channelLabel);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(User record);

	User selectByNameAndChannel(@Param("username") String username, @Param("channel") String channel);

	int updateServers(HashMap<String, String> map);

	Long selectMaxId();

}