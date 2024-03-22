package cn.game.login.mapper;

import java.util.HashMap;

import org.apache.ibatis.annotations.Param;

import cn.game.login.cache.entity.User;

public interface UserMapper {
	int deleteByPrimaryKey(Long id);

	int insert(User record);

	int insertSelective(User record);

	User selectByPrimaryKey(Long id);

	int updateByPrimaryKeySelective(User record);

	int updateByPrimaryKey(User record);

	User selectByNameAndChannel(@Param("username") String username, @Param("channel") String channel);

	int updateServers(HashMap<String, String> map);

}