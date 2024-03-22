package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.Account;

public interface AccountMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int insert(Account row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Account row);

	/**
	 * @mbg.generated
	 */
	Account selectByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Account row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Account row);
}