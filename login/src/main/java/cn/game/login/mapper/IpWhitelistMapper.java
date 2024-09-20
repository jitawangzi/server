package cn.game.login.mapper;

import java.util.List;

import cn.game.login.cache.entity.IpWhitelist;
import org.apache.ibatis.annotations.Param;

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
	int insertBatch(List<IpWhitelist> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<IpWhitelist> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<IpWhitelist> recordList);

	public List<IpWhitelist> selectAll();
}