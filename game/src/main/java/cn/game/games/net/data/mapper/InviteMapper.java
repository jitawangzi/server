package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.Invite;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface InviteMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int insert(Invite row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Invite row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Invite row);

	/**
	 * @mbg.generated
	 */
	Invite selectByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Invite row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Invite row);

	/**
	 * @mbg.generated
	 */
	List<Invite> selectByIndexPid(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	List<Invite> selectAll();

	/**
	 * @mbg.generated
	 */
	List<Invite> getBatch(@Param("offset") int offset, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<Invite> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<Invite> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<Invite> recordList);
}