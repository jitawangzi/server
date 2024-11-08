package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.Mail;

public interface MailMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int insert(Mail row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Mail row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Mail row);

	/**
	 * @mbg.generated
	 */
	Mail selectByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Mail row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Mail row);

	/**
	 * @mbg.generated
	 */
	List<Mail> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	List<Mail> selectAll();

	/**
	 * @mbg.generated
	 */
	List<Mail> getBatch(@Param("offset") int offset, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<Mail> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<Mail> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<Mail> recordList);
}