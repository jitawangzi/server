package cn.game.games.net.data.mapper;

import java.util.List;

import cn.game.games.cache.entity.Member;
import org.apache.ibatis.annotations.Param;

public interface MemberMapper {
    /**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long playerId);

	/**
	 * @mbg.generated
	 */
	int insert(Member row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Member row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Member row);

	/**
	 * @mbg.generated
	 */
	Member selectByPrimaryKey(Long playerId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Member row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeyWithBLOBs(Member row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Member row);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<Member> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<Member> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<Member> recordList);

	List<Member> selectAll();
}