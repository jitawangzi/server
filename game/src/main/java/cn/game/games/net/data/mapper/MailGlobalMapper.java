package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.MailGlobal;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MailGlobalMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int insert(MailGlobal row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(MailGlobal row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(MailGlobal row);

	/**
	 * @mbg.generated
	 */
	MailGlobal selectByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(MailGlobal row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(MailGlobal row);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<MailGlobal> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<MailGlobal> records);

	/**
	 * @mbg.generated
	 */
	int batchUpdate(@Param("recordList") List<MailGlobal> recordList);
}