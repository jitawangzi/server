package cn.game.login.mapper;

import cn.game.login.cache.entity.PayOrder;

public interface PayOrderMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int insert(PayOrder row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(PayOrder row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(PayOrder row);

	/**
	 * @mbg.generated
	 */
	PayOrder selectByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(PayOrder row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeyWithBLOBs(PayOrder row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(PayOrder row);
}