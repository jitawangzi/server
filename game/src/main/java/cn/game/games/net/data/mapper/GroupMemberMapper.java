package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.GroupMember;

public interface GroupMemberMapper {
    /**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("groupId") Long groupId, @Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insert(GroupMember row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(GroupMember row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(GroupMember row);

	/**
	 * @mbg.generated
	 */
	GroupMember selectByPrimaryKey(@Param("groupId") Long groupId, @Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(GroupMember row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(GroupMember row);

	/**
	 * @mbg.generated
	 */
	List<GroupMember> selectByGroupId(@Param("groupId") Long groupId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<GroupMember> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<GroupMember> records);

	List<GroupMember> selectAll();
	
	int deleteByGroupKey(Long groupId);
}