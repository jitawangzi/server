package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.UserTag;

public interface UserTagMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("uid") Long uid, @Param("tagId") Integer tagId);

	/**
	 * @mbg.generated
	 */
	int insert(UserTag row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(UserTag row);

	/**
	 * @mbg.generated
	 */
	UserTag selectByPrimaryKey(@Param("uid") Long uid, @Param("tagId") Integer tagId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(UserTag row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(UserTag row);

	/**
	 * @mbg.generated
	 */
	List<UserTag> selectByUid(@Param("uid") Long uid);

}