package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.User;

public interface UserMapper {

	/**
	 * @mbggenerated
	 */
	int deleteByPrimaryKey(Long id);

	/**
	 * @mbggenerated
	 */
	int insert(User record);

	/**
	 * @mbggenerated
	 */
	int insertSelective(User record);

	/**
	 * @mbggenerated
	 */
	User selectByPrimaryKey(Long id);

	/**
	 * @mbggenerated
	 */
	int updateByPrimaryKeySelective(User record);

	/**
	 * @mbggenerated
	 */
	int updateByPrimaryKey(User record);
}