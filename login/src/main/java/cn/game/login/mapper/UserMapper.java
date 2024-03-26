package cn.game.login.mapper;

import java.util.HashMap;

import org.apache.ibatis.annotations.Param;

import cn.game.login.cache.entity.User;

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
	int insertOrUpdate(User row);

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

	User selectByNameAndChannel(@Param("username") String username, @Param("channel") String channel);

	int updateServers(HashMap<String, String> map);

}