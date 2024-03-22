package cn.game.games.net.game.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.games.cache.entity.Group;
import cn.game.games.cache.entity.GroupMember;
import cn.game.games.cache.entity.PlayerGroup;
import cn.game.games.cache.entity.PlayerGroupApplication;
import cn.game.games.net.data.mapper.GroupMapper;
import cn.game.games.net.data.mapper.GroupMemberMapper;
import cn.game.games.net.data.mapper.PlayerGroupApplicationMapper;
import cn.game.games.net.data.mapper.PlayerGroupMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.util.DAO;

public class ChatHelper {
	private static final Logger log = LoggerFactory.getLogger(ChatHelper.class);
	
	
	public static void insertGroup(Group group) {
		DAO.execute(GroupMapper.class,
				MapperConstant.insert, group);
	}
	
	public static void deleteGroup(long groupId) {
		DAO.execute(GroupMapper.class,
				MapperConstant.deleteByPrimaryKey, groupId);
	}
	
	public static void updateGroup(Group group) {
		DAO.execute(GroupMapper.class,
				MapperConstant.updateByPrimaryKey, group);
	}
	
	public static void insertGroupMember(GroupMember groupMember) {
		DAO.execute(GroupMemberMapper.class,
				MapperConstant.insert, groupMember);
	}
	
	public static void deleteGroupMember(long groupId) {
		DAO.execute(GroupMemberMapper.class,
				"deleteByGroupKey", groupId);
	}
	
	public static void deleteGroupMemberByMap(Object[] ids) {
		DAO.execute(GroupMemberMapper.class,
				MapperConstant.deleteByPrimaryKey, ids);
	}
	
	public static void insertPlayerGroup(PlayerGroup playerGroup) {
		DAO.execute(PlayerGroupMapper.class,
				MapperConstant.insert, playerGroup);
	}
	
	public static void deletePlayerGroupByMap(Object[] ids) {
		DAO.execute(PlayerGroupMapper.class,
				MapperConstant.deleteByPrimaryKey, ids);
	}
	
	public static void insertPlayerGroupApplication(PlayerGroupApplication playerGroupApplication) {
		DAO.execute(PlayerGroupApplicationMapper.class,
				MapperConstant.insert, playerGroupApplication);
	}
	
	public static void deletePlayerGroupApplication(Object[] ids) {
		DAO.execute(PlayerGroupApplicationMapper.class,
				MapperConstant.deleteByPrimaryKey, ids);
	}
}
