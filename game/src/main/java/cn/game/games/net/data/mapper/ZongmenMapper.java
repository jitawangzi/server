package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.Zongmen;

public interface ZongmenMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int insert(Zongmen row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Zongmen row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Zongmen row);

	/**
	 * @mbg.generated
	 */
	Zongmen selectByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Zongmen row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeyWithBLOBs(Zongmen row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Zongmen row);

	/**
	 * @mbg.generated
	 */
	List<Zongmen> selectByServerNodeIdIndex(@Param("serverNodeId") String serverNodeId);

	/**
	 * @mbg.generated
	 */
	List<Zongmen> selectAll();

	/**
	 * @mbg.generated
	 */
	List<Zongmen> getBatchOffset(@Param("offset") int offset, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	List<Zongmen> getBatchCursor(@Param("lastId") Long lastId, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	long getTotal();

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<Zongmen> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<Zongmen> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<Zongmen> recordList);

	/**
	 * @mbg.generated
	 */
	Long getLastIdOfBatch(@Param("lastId") Long lastId, @Param("limit") int limit);

}