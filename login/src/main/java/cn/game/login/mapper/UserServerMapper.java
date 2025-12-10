package cn.game.login.mapper;

import cn.game.login.cache.entity.UserServer;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserServerMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int insert(UserServer row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(UserServer row);

	/**
	 * @mbg.generated
	 */
	UserServer selectByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(UserServer row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(UserServer row);

	/**
	 * @mbg.generated
	 */
	List<UserServer> selectAll();

	/**
	 * @mbg.generated
	 */
	List<UserServer> getBatchOffset(@Param("offset") int offset, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	List<UserServer> getBatchCursor(@Param("lastId") Long lastId, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	List<UserServer> selectByUserId(@Param("userId") Long userId);

	/**
	 * @mbg.generated
	 */
	UserServer selectByUkUserServer(@Param("userId") Long userId, @Param("serverId") String serverId);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(UserServer record);
}