package cn.game.login.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.login.cache.entity.IpWhitelist;

public interface IpWhitelistMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Integer id);

	/**
	 * @mbg.generated
	 */
	int insert(IpWhitelist row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(IpWhitelist row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(IpWhitelist row);

	/**
	 * @mbg.generated
	 */
	IpWhitelist selectByPrimaryKey(Integer id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(IpWhitelist row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(IpWhitelist row);

	/**
	 * @mbg.generated
	 */
	List<IpWhitelist> selectAll();

	/**
	 * @mbg.generated
	 */
	List<IpWhitelist> getBatchOffset(@Param("offset") int offset, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	List<IpWhitelist> getBatchCursor(@Param("lastId") Integer lastId, @Param("limit") int limit);
}