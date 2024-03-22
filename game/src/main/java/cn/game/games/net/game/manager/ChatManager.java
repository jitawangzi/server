package cn.game.games.net.game.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import cn.game.core.base.ServerContext;
import cn.game.core.util.IdUtil;
import cn.game.games.cache.entity.Group;
import cn.game.games.cache.entity.GroupMember;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerGroup;
import cn.game.games.cache.entity.PlayerGroupApplication;
import cn.game.games.cache.op.impl.FriendOp;
import cn.game.games.core.SimplePlayer;
import cn.game.games.net.data.mapper.GroupMapper;
import cn.game.games.net.data.mapper.GroupMemberMapper;
import cn.game.games.net.data.mapper.PlayerGroupApplicationMapper;
import cn.game.games.net.data.mapper.PlayerGroupMapper;
import cn.game.games.net.game.GameServer;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.helper.ChatHelper;
import cn.game.games.util.DAO;
import cn.game.games.util.PbBuilder;
import cn.game.protocol.generated.config.OldGlobalConst;
import cn.game.protocol.manual.OldErrorMsgEnum;
import cn.game.protocol.protobuf.BaseMsg.SimplePlayerInfo;
import cn.game.protocol.protobuf.ChatMsg.ChatAgreeGroupInvitationPush_51000060;
import cn.game.protocol.protobuf.ChatMsg.ChatAgreeGroupOtherServerPush_51000053;
import cn.game.protocol.protobuf.ChatMsg.ChatGroupBriefInfo;
import cn.game.protocol.protobuf.ChatMsg.ChatGroupDissolutionPush_51000030;
import cn.game.protocol.protobuf.ChatMsg.ChatGroupInfo;
import cn.game.protocol.protobuf.ChatMsg.ChatGroupInviteInfo;
import cn.game.protocol.protobuf.ChatMsg.ChatGroupQuitPush_51000035;
import cn.game.protocol.protobuf.ChatMsg.ChatInviteMePush_51000065;
import cn.game.protocol.protobuf.ChatMsg.ChatOutGroupPush_51000055;
import cn.game.protocol.protobuf.ChatMsg.ChatOutGroupType;
import cn.game.protocol.protobuf.ChatMsg.ChatPrivateOtherServerPush_51000057;
import cn.game.protocol.protobuf.ChatMsg.ChatSendOtherServerGroupPush_51000056;
import cn.game.protocol.protobuf.ChatMsg.ChatSendPush_51000040;
import cn.game.protocol.protobuf.ChatMsg.ChatSystemPush_51000050;
import cn.game.protocol.protobuf.ChatMsg.ChatType;
import cn.game.util.Config;

public class ChatManager {
	// 聊天管理实例
	private static ChatManager instance = new ChatManager();
	// 所有群组信息 群组id 群组信息
	private static ConcurrentMap<Long, Group> groupInfos = new ConcurrentHashMap<>();
	// 所有群组成员信息 玩家id 玩家所有群组id
	private static ConcurrentMap<Long, List<PlayerGroup>> playerGroupInfos = new ConcurrentHashMap<>();
	// 所有群组成员信息 群组id 群组所有玩家id
	private static ConcurrentMap<Long, List<GroupMember>> groupMemberInfos = new ConcurrentHashMap<>();
	// 所有玩家的群组邀请入群审批列表 玩家id value 群组id 群组邀请信息
	private static ConcurrentMap<Long, Map<Long, PlayerGroupApplication>> playerGroupApplications = new ConcurrentHashMap<>();

	// 一个玩家最大创建群组数量
	private static int playerCreateGroupMaxCount = 0;

	// 一个玩家最大加入群组数量 （不与创建共享）
	private static int playerJohnGroupMaxCount = 0;
	
	// 可收到群邀请的最大数量
	private static int maxGroupApplyCount = 0;
	
	// 群组最大人数上限
	private static int groupMaxMemberCount = 0;

	// 获取聊天管理实例
	public static ChatManager getInstance() {
		return instance;
	}

	// 初始化加载群组和群组成员、玩家群组、玩家审批群组邀请信息
	
	public void init() {
		// 查询群组信息
		List<Group> groups = (List<Group>) DAO.executeSync(GroupMapper.class, MapperConstant.selectAll, null);

		if(groups != null) {
			for (Group group : groups) {
				groupInfos.put(group.getId(), group);
			}
		}
		

		// 查询群组成员信息
		List<GroupMember> groupMembers = (List<GroupMember>) DAO.executeSync(GroupMemberMapper.class,
				MapperConstant.selectAll, null);

		if(groupMembers != null) {
			for (GroupMember groupMember : groupMembers) {

				if (hasGroupMember(groupMember.getGroupId())) {
					groupMemberInfos.get(groupMember.getGroupId()).add(groupMember);
				} else {
					List<GroupMember> members = new ArrayList<>();
					members.add(groupMember);
					groupMemberInfos.put(groupMember.getGroupId(), members);
				}
			}
		}
		
		// 查询玩家所有群组信息
		List<PlayerGroup> playerGroupLists = (List<PlayerGroup>) DAO.executeSync(PlayerGroupMapper.class,
				MapperConstant.selectAll, null);

		if(playerGroupLists != null) {
			for (PlayerGroup playerGroup : playerGroupLists) {
				if (hasPlayerGroup(playerGroup.getPlayerId())) {
					playerGroupInfos.get(playerGroup.getPlayerId()).add(playerGroup);
				} else {
					List<PlayerGroup> myGroups = new ArrayList();
					myGroups.add(playerGroup);
					playerGroupInfos.put(playerGroup.getPlayerId(), myGroups);
				}
			}
		}
		
		// 查询玩家群组邀请审批列表信息
		List<PlayerGroupApplication> pGroupApplicationsLists = (List<PlayerGroupApplication>) DAO
				.executeSync(PlayerGroupApplicationMapper.class, MapperConstant.selectAll, null);
		
		if(pGroupApplicationsLists != null) {
			for (PlayerGroupApplication pApplication : pGroupApplicationsLists) {
				if (hasGroupApplications(pApplication.getPlayerId())) {
					playerGroupApplications.get(pApplication.getPlayerId()).put(pApplication.getGroupId(), pApplication);
				} else {
					Map<Long, PlayerGroupApplication> myApplications = new ConcurrentHashMap<>();
					myApplications.put(pApplication.getGroupId(), pApplication);
					playerGroupApplications.put(pApplication.getPlayerId(), myApplications);
				}
			}
		}

		playerCreateGroupMaxCount = OldGlobalConst.groupConst.get(0);
		playerJohnGroupMaxCount = OldGlobalConst.groupConst.get(1);
		maxGroupApplyCount = OldGlobalConst.groupConst.get(2);
		groupMaxMemberCount = OldGlobalConst.groupConst.get(3);
	}

	/**
	 * 是否有此群组
	 * @param gourpId
	 * @return
	 */
	public boolean hasGroup(long groupId) {return groupInfos.containsKey(groupId);}
	
	/**
	 * 是否有此群组成员信息
	 * @param groupId
	 * @return
	 */
	public boolean hasGroupMember(long groupId) {return groupMemberInfos.containsKey(groupId);}
	
	/**
	 * 是否有此玩家的群组信息
	 * @param playerId
	 * @return
	 */
	public boolean hasPlayerGroup(long playerId) {return playerGroupInfos.containsKey(playerId);}
	
	/**
	 * 是否有此玩家的被邀请入群信息
	 * @param playerId
	 * @return
	 */
	public boolean hasGroupApplications(long playerId) {return playerGroupApplications.containsKey(playerId);}
	
	/**
	 * 获取一个群组的信息
	 * 
	 * @param groupId
	 * @return
	 */
	public Group getOneGroupById(long groupId) {
		if (hasGroup(groupId)) {
			return groupInfos.get(groupId);
		}

		return null;
	}

	/**
	 * 发送系统消息
	 * 
	 * @param word
	 */
	public void sendOneSystemChat(String word) {
		ChatSystemPush_51000050.Builder response = ChatSystemPush_51000050.newBuilder();
		response.setContent(word);
		GameClientManager.getInstance().broadcast(response);
	}

	/**
	 * 发送私聊消息 本服
	 * 
	 * @param playerId
	 * @param word
	 * @param replyWord
	 * @param client
	 */
	public OldErrorMsgEnum sendPrivateChat(long playerId, String word, String replyWord, long myPlayerId) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		if (player == null) {
			return OldErrorMsgEnum.player_not_online;
		}
		
		// 判断自己是否是对方黑名单
		if(checkBlack(playerId, myPlayerId)) {
			return OldErrorMsgEnum.black_friend_chat_not;
		}
		// 私聊的对象的简略信息
		SimplePlayerInfo sPlayerInfoOther = PbBuilder.buildSimplePlayerInfo(player);

//					Config.checkText(word);
		// 自己的简略信息
		Player playerMe = PlayerManager.getInstance().getPlayer(myPlayerId);
		SimplePlayerInfo sPlayerInfoMe = PbBuilder.buildSimplePlayerInfo(playerMe);

		String serverId = ServerContext.getInstance().getServerId();

		// 发给私聊对象，自己的简略信息
		sendChat(myPlayerId, word, replyWord, sPlayerInfoMe, playerId, serverId);
		// 发给自己 私聊对象的简略信息
		sendChat(myPlayerId, word, replyWord, sPlayerInfoOther, myPlayerId, serverId);

		return OldErrorMsgEnum.ok;
	}
	
	/**
	 * 判断自己是否被人拉黑了
	 * @param playerId
	 * @param myplayerId
	 * @return
	 */
	public boolean checkBlack(long playerId, long myplayerId) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		FriendOp friendOp = player.getModule(FriendOp.class);
		return friendOp.isBlack(myplayerId);
	}

	/**
	 * 发送群组消息 本服 和 跨服
	 * 
	 * @param groupId
	 * @param word
	 * @param replyWord
	 * @param client
	 */
	public OldErrorMsgEnum sendGroupChat(long groupId, String word, String replyWord, long myPlayerId) {		
//		Config.checkText(word);

		if(!hasPlayerGroup(myPlayerId))
			return OldErrorMsgEnum.unknown;
		
		PlayerGroup playerGroup = null;
		for (PlayerGroup pGroup : playerGroupInfos.get(myPlayerId)) {
			if(pGroup.getGroupId().longValue() == groupId)
			{
				playerGroup = pGroup;
				break;
			}
		}
		
		if(playerGroup == null)
			return OldErrorMsgEnum.unknown;
		
		Player me = PlayerManager.getInstance().getPlayer(myPlayerId);
		SimplePlayerInfo sPlayerInfo = PbBuilder.buildSimplePlayerInfo(me);
		
		// 本服
		if(GameServer.getInstance().isLocalServer(playerGroup.getGroupServerId())) {									
			sendGroupChat(sPlayerInfo, groupId, word, replyWord);
			
		}else {
			// 转发给那个跨服的群组 因为群组信息在那个服务器上，所以要转发，然后那个服务器发给所有的群玩家
			ChatSendOtherServerGroupPush_51000056.Builder response = ChatSendOtherServerGroupPush_51000056.newBuilder();
			response.setPlayerInfo(sPlayerInfo);
			response.setGroupId(groupId + "");
			response.setWord(word);
			response.setReplyWord(replyWord);
			
			GameClientManager.getInstance().sendToGameServer(playerGroup.getGroupServerId(), response.build());			
		}				
		
		return OldErrorMsgEnum.ok;
	}
	
	/**
	 * 发送一个群组聊天消息
	 * @param sPlayerInfo
	 * @param groupId
	 * @param word
	 * @param replyWord
	 * @param playerIdLists
	 * @param playerServerIds
	 */
	public void sendGroupChat(SimplePlayerInfo sPlayerInfo, long groupId, String word, String replyWord) {
		if (!hasGroupMember(groupId)) {
			return;
		}

		if (groupMemberInfos.get(groupId).size() < 1) {
			return;
		}
		
		List<Long> playerIdLists = new ArrayList<>();
		List<String> playerServerIds = new ArrayList<>();

		for (GroupMember member : groupMemberInfos.get(groupId)) {
			playerIdLists.add(member.getPlayerId());
			playerServerIds.add(member.getPlayerServerId().toString());
		}	
		
		ChatSendPush_51000040.Builder responseGroup = ChatSendPush_51000040.newBuilder();
		responseGroup.setType(ChatType.GROUP);
		responseGroup.setId(groupId + "");
		responseGroup.setContent(word);
		responseGroup.setBriefInfo(sPlayerInfo);
		responseGroup.setReplyContent(replyWord);
		
		// 遍历在线组员 包括自己 因为自己发的话也要检查是否合法
		GameClientManager.getInstance().broadcast(responseGroup.build(), playerIdLists, playerServerIds);
	}

	/**
	 * 发送跨服私聊
	 * 
	 * @param playerId
	 * @param word
	 * @param replyWord
	 * @param client
	 * @param serverId
	 */
	public OldErrorMsgEnum sendOtherServerPrivateChat(long playerId, String word, String replyWord, long myPlayerId,
			String serverId) {
		OldErrorMsgEnum errorMsgEnum = OldErrorMsgEnum.player_not_online;
		try {
			// 获取跨服玩家的简略信息
			SimplePlayer simplePlayer = PlayerManager.getInstance().getAndLoadSimplePlayer(playerId, serverId);
			
			if(simplePlayer == null)
				return errorMsgEnum;

			if (!simplePlayer.isOnline())
				return errorMsgEnum;
			
			// 转发
			Player playerMe = PlayerManager.getInstance().getPlayer(myPlayerId);
			// 自己的简略信息
			SimplePlayerInfo sPlayerInfoMe = PbBuilder.buildSimplePlayerInfo(playerMe);
			
			ChatPrivateOtherServerPush_51000057.Builder response = ChatPrivateOtherServerPush_51000057.newBuilder();
			response.setWord(word);
			response.setPlayerInfo(sPlayerInfoMe);
			response.setReplyWord(replyWord);
			response.setPlayerId(playerId + "");

			GameClientManager.getInstance().sendToGameServer(serverId, response.build());
			// 私聊对象的简略信息
//			SimplePlayerInfo sPlayerInfoOther = PbBuilder.buildSimplePlayerInfo(simplePlayer);

			// 发给私聊对象，自己的简略信息
//			sendChat(myPlayerId, word, replyWord, sPlayerInfoMe, playerId, serverId);
			// 发给自己，私聊对象的简略信息
//			sendChat(myPlayerId, word, replyWord, sPlayerInfoOther, myPlayerId, ServerContext.getInstance().getServerId());
			
			errorMsgEnum = OldErrorMsgEnum.ok;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return errorMsgEnum;
	}

	/**
	 * 发送聊天消息 本服 加 跨服
	 * 
	 * @param type
	 * @param playerId
	 * @param word
	 * @param replyWord
	 * @param sPlayerInfo
	 */
	public void sendChat(long id, String word, String replyWord, SimplePlayerInfo sPlayerInfo, long sendPlayerId,
			String sendServerId) {
		ChatSendPush_51000040.Builder response = ChatSendPush_51000040.newBuilder();
		response.setType(ChatType.PRIVATE);
		response.setId(id + "");
		response.setContent(word);
		response.setBriefInfo(sPlayerInfo);
		response.setReplyContent(replyWord);

		if (GameServer.getInstance().isLocalServer(sendServerId)) {
			GameClientManager.getInstance().noticeOne(response.build(), sendPlayerId);
		} else {
			GameClientManager.getInstance().sendToRemotePlayer(sendPlayerId, sendServerId, response.build());
		}

	}

	/**
	 * 创建一个群组
	 * 
	 * @param groupName
	 * @param headId
	 * @param notice
	 * @param client
	 */
	public ChatGroupBriefInfo createOneGroup(String groupName, int headId, String notice, long myPlayerId) {		
		try {
			Group newGroup = new Group();

			long newGroupId = IdUtil.getId();
			newGroup.setId(newGroupId);
			newGroup.setName(groupName);
			newGroup.setHeadIcon(headId);
			newGroup.setNotice(notice);
			newGroup.setManagerId(myPlayerId);

			// 创建成员
			GroupMember groupMember = new GroupMember();
			groupMember.setGroupId(newGroup.getId());
			groupMember.setPlayerId(myPlayerId);
			groupMember.setPlayerServerId(ServerContext.getInstance().getServerId());

			// 加入群组
			groupInfos.put(newGroup.getId(), newGroup);

			// 加入玩家的群组列表
			PlayerGroup playerGroup = new PlayerGroup();
			playerGroup.setGroupId(newGroup.getId());
			playerGroup.setGroupServerId(ServerContext.getInstance().getServerId());
			playerGroup.setPlayerId(myPlayerId);

			if (hasPlayerGroup(myPlayerId)) {
				playerGroupInfos.get(myPlayerId).add(playerGroup);
			} else {
				List<PlayerGroup> playerGroups = new ArrayList<>();
				playerGroups.add(playerGroup);
				playerGroupInfos.put(myPlayerId, playerGroups);
			}

			// 加入群组的玩家列表
			List<GroupMember> members = new ArrayList<>();
			members.add(groupMember);
			groupMemberInfos.put(newGroup.getId(), members);

			// 存入DB 群组表 成员表 玩家群组表
			ChatHelper.insertGroup(newGroup);
			ChatHelper.insertGroupMember(groupMember);
			ChatHelper.insertPlayerGroup(playerGroup);

			return getGroupBriefInfoById(newGroupId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 校验可创建群组的数量是否足够 返回错误码 null为足够
	 * 
	 * @param playerId
	 * @return
	 */
	public boolean checkCreateGroupCount(long playerId) {
		// 校验创建数量
		int createCount = 0;
		if (hasPlayerGroup(playerId)) {
			for (PlayerGroup myGroup : playerGroupInfos.get(playerId)) {				
				if(GameServer.getInstance().isLocalServer(myGroup.getGroupServerId())) {
					if (groupInfos.get(myGroup.getGroupId()).getManagerId().longValue() == playerId) {
						createCount++;
					}
				}				
			}
		}

		// 校验创建数量
		if (createCount + 1 > playerCreateGroupMaxCount) {
			return false;
		}
		return true;
	}
	
	/**
	 * 退出群组 本服
	 * 
	 * @param groupId
	 * @param serverId
	 * @param playerId
	 * @return
	 */
	public boolean quitGroup(long groupId, String serverId, long playerId) {
		if (serverId.equals(ServerContext.getInstance().getServerId())) {
			if (!hasGroup(groupId))
				return false;

			if (!hasPlayerGroup(playerId))
				return false;

			// 不在群组里就无法退出
			if (!checkMemberGroup(groupId, playerId))
				return false;

			Group myGroup = groupInfos.get(groupId);
			
			sendAllGroupPlayerRemove(groupId, playerId, true);

			// 如果是群主 那就是解散群组
			if (myGroup.getManagerId().longValue() == playerId) {

				// 删除群组信息
				ChatHelper.deleteGroup(groupId);

				// 删除成员表的信息
				ChatHelper.deleteGroupMember(groupId);

				ChatGroupDissolutionPush_51000030.Builder dissResponse = ChatGroupDissolutionPush_51000030.newBuilder();
				dissResponse.setGroupId(groupId + "");
				dissResponse.setServerId(serverId);

				// 遍历成员 修改群组成员的信息
				for (GroupMember member : groupMemberInfos.get(groupId)) {
					if (member.getPlayerServerId().equals(ServerContext.getInstance().getServerId())) {
						if (hasPlayerGroup(member.getPlayerId())) {
							removePlayerGroup(member.getPlayerId(), groupId);
							// 通知解散了群组
							GameClientManager.getInstance().noticeOne(dissResponse.build(), member.getPlayerId());
						}
					} else {
						// 跨服 要走对方服务器，然后删除玩家拥有群组对应信息 再通知在线的玩家
						ChatOutGroupPush_51000055.Builder response = ChatOutGroupPush_51000055.newBuilder();
						response.setPlayerId(member.getPlayerId().toString());
						response.setGroupId(groupId + "");
						response.setOutType(ChatOutGroupType.DISSOLUTION_OUT);
						
						GameClientManager.getInstance().sendToGameServer(member.getPlayerServerId(), response.build());
					}
				}

				// 这里直接都删除了，所以不需要先删除群组的信息
				groupMemberInfos.remove(groupId);
				groupInfos.remove(groupId);
			} else {
				// 校验完毕，开始退出群组 				
				playerOutGroup(groupId, playerId);
			}

			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 玩家退出群组 先发消息说退出群组，这样自己也能提示到
	 * @param groupId
	 * @param playerId
	 */
	public void playerOutGroup(long groupId, long playerId) {
		removePlayerGroup(playerId, groupId);
		removeGroupMember(playerId, groupId);
	}

	/**
	 * 删除一个玩家的群组 包括跨服
	 * 
	 * @param onePlayerId
	 * @param groupId
	 */
	public void removePlayerGroup(long onePlayerId, long groupId) {
		Object[] ids = new Object[] { onePlayerId, groupId };

		Iterator<PlayerGroup> itemIterator = playerGroupInfos.get(onePlayerId).iterator();
		while (itemIterator.hasNext()) {
			PlayerGroup playerOneGroup = itemIterator.next();
			if (playerOneGroup.getGroupId().longValue() == groupId) {
				ChatHelper.deletePlayerGroupByMap(ids);
				itemIterator.remove();
				return;
			}
		}		
	}
	
	/**
	 * 删除群组的玩家 包括跨服
	 * @param onePlayerId
	 * @param groupId
	 */
	public void removeGroupMember(long onePlayerId, long groupId) {
		Object[] ids = new Object[] { groupId ,onePlayerId };
		
		Iterator<GroupMember> itemIterator = groupMemberInfos.get(groupId).iterator();
		while (itemIterator.hasNext()) {
			GroupMember gMember = itemIterator.next();
			if (gMember.getPlayerId().longValue() == onePlayerId) {
				ChatHelper.deleteGroupMemberByMap(ids);
				itemIterator.remove();
				return;
			}
		}
	}

	/**
	 * 通知一个群组的人(包含退出或被踢的人)，有人退出或者被踢出了群组
	 * 
	 * @param groupId
	 * @param playerId
	 * @param isVoluntarily
	 */
	public void sendAllGroupPlayerRemove(long groupId, long playerId, boolean isVoluntarily) {
		ChatGroupQuitPush_51000035.Builder quitResponse = ChatGroupQuitPush_51000035.newBuilder();
		quitResponse.setGroupId(groupId + "");
		quitResponse.setPlayerId(playerId + "");
		quitResponse.setIsVoluntarily(isVoluntarily);

		for (GroupMember member : groupMemberInfos.get(groupId)) {
			if (member.getPlayerServerId().equals(ServerContext.getInstance().getServerId())) {
				// 通知群组退出消息
				GameClientManager.getInstance().noticeOne(quitResponse.build(), member.getPlayerId());
			} else {
				// 跨服
				GameClientManager.getInstance().sendToRemotePlayer(playerId, member.getPlayerServerId(), quitResponse.build());
			}
		}
	}

	/**
	 * 群主踢出一个玩家
	 * 
	 * @param groupId
	 * @param playerId
	 * @param serverId
	 * @return
	 */
	public boolean expelMember(long groupId, long playerId, String serverId, long myPlayerId) {

		if (!hasGroup(groupId))
			return false;

		if (!checkMemberGroup(groupId, playerId))
			return false;

		// 判断是否有权限，群组才可以踢人
		if (!checkGroupJurisdiction(groupId, myPlayerId))
			return false;
		
		// 开始踢人 先发踢人消息，这样被踢的人可以收到消息		
		sendAllGroupPlayerRemove(groupId, playerId, false);
		if(GameServer.getInstance().isLocalServer(serverId)) {
			playerOutGroup(groupId, playerId);
		}else {
			// 跨服 先清本服的群组玩家 再转发协议 跨服删玩家群组信息
			removeGroupMember(playerId, groupId);
			ChatOutGroupPush_51000055.Builder response = ChatOutGroupPush_51000055.newBuilder();
			response.setPlayerId(playerId + "");
			response.setGroupId(groupId + "");
			response.setOutType(ChatOutGroupType.KICK_OUT);
			
			GameClientManager.getInstance().sendToGameServer(serverId, response.build());
		}		
		
		return true;
	}

	/**
	 * 处理一个被邀请入群请求 本服和跨服
	 * 
	 * @param ChatGroupBriefInfo 发起邀请的群组信息
	 * @param playerId           受邀人id
	 * @param serverId
	 * @param myPlayerId         邀请人的玩家id
	 * @param myName             邀请人的名字
	 * @return
	 */
	public boolean inviteOnePlayerGroup(ChatGroupBriefInfo groupBriefInfo, long playerId, long myPlayerId,
			String myName) {
		long groupId = Long.parseLong(groupBriefInfo.getId());		
		
		// 检查玩家是否可加入的群组数量已满
		if (checkPlayerGroupCount(playerId) + 1 > playerJohnGroupMaxCount)
			return false;

		// 查看是否已经被邀请过了
		if (hasGroupApplications(playerId)) {
			if (playerGroupApplications.get(playerId).containsKey(groupId))
				return false;
			
			if(playerGroupApplications.get(playerId).size() + 1 > maxGroupApplyCount)
				return false;
						
		}				
		
		if (groupBriefInfo.getMemberCount() + 1 > groupMaxMemberCount) {
			return false;
		}
		

		// 邀请入群 如果在线发协议，不在线则只写审批列表
		ChatInviteMePush_51000065.Builder response = ChatInviteMePush_51000065.newBuilder();
		response.setGroupInfo(groupBriefInfo);
		response.setName(myName);

		PlayerGroupApplication playerGroupApplication = new PlayerGroupApplication();
		playerGroupApplication.setGroupId(groupId);
		playerGroupApplication.setPlayerId(playerId);
		playerGroupApplication.setServerId(groupBriefInfo.getServerId());
		playerGroupApplication.setFriendPlayerId(myPlayerId);

		if (!hasGroupApplications(playerId)) {
			Map<Long, PlayerGroupApplication> pApplications = new ConcurrentHashMap<>();
			pApplications.put(groupId, playerGroupApplication);
			playerGroupApplications.put(playerId, pApplications);
		} else {
			playerGroupApplications.get(playerId).put(groupId, playerGroupApplication);
		}

		// 存入DB
		ChatHelper.insertPlayerGroupApplication(playerGroupApplication);

		GameClientManager.getInstance().noticeOne(response.build(), playerId);
		return true;
	}
	
	/**
	 * 校验一个玩家是否可以加入一个群组
	 * @param myPlayerId
	 * @param groupId
	 * @return
	 */
	private OldErrorMsgEnum checkOnePlayerJoinGroup(long myPlayerId, long groupId) {
		// 校验玩家要加入群组 是否合法
		if (hasPlayerGroup(myPlayerId)) {
			if (checkPlayerGroupCount(myPlayerId) + 1 > playerJohnGroupMaxCount)
				return OldErrorMsgEnum.not_player_group_count;

			for (PlayerGroup oneGroup : playerGroupInfos.get(myPlayerId)) {
				if (oneGroup.getGroupId().longValue() == groupId)
					return OldErrorMsgEnum.not_player_group;
			}
		}

		// 校验申请信息是否存在
		if (!hasGroupApplications(myPlayerId))
			return OldErrorMsgEnum.unknown;
		else {
			if (!playerGroupApplications.get(myPlayerId).containsKey(groupId))
				return OldErrorMsgEnum.unknown;
		}					
		
		return OldErrorMsgEnum.ok;
	}
	
	/**
	 * 玩家点击同意入群或不同意 本服
	 * 
	 * @param myPlayerId 受邀人
	 * @param groupId 群组id
	 * @param serverId 区服id
	 * @param isAgree 是否同意
	 * @return
	 */
	public OldErrorMsgEnum agreeOneGroupInvitation(long myPlayerId, long groupId, String serverId, boolean isAgree) {
		OldErrorMsgEnum errorMsgEnum = OldErrorMsgEnum.ok;		
		
		// 同意
		if (isAgree) {	
			errorMsgEnum = checkOnePlayerJoinGroup(myPlayerId, groupId);
			if(errorMsgEnum != OldErrorMsgEnum.ok)
				return errorMsgEnum;
			
			errorMsgEnum = joinOneGroup(myPlayerId, groupId, serverId, isAgree, true);
			if(errorMsgEnum != OldErrorMsgEnum.ok)
				return errorMsgEnum;
		}
								
		// 删除邀请信息 和发 同意或不同意消息		
		Player player = PlayerManager.getInstance().getPlayer(myPlayerId);
		long friendPlayerId = playerGroupApplications.get(myPlayerId).get(groupId).getFriendPlayerId();
		sendPlayerIsAgreeMessage(player.getData().getName(), groupId, isAgree, friendPlayerId);
		deleteOneGroupApplication(myPlayerId, groupId);
		return errorMsgEnum;
	}
	
	/**
	 * 玩家同意或不同意其它区服的入群邀请 去除自己服的自己的入群邀请信息，然后给对方服务器反馈是否同意
	 * @param myPlayerId
	 * @param groupId
	 * @param serverId
	 * @param isAgree
	 */
	public OldErrorMsgEnum agreeOneOtherGroupInvitation(long myPlayerId, long groupId, String serverId, boolean isAgree) {
		if(isAgree) {
			OldErrorMsgEnum errorMsgEnum = checkOnePlayerJoinGroup(myPlayerId, groupId);
			if(errorMsgEnum != OldErrorMsgEnum.ok)
				return errorMsgEnum;
		}
		
		long friendPlayerId = playerGroupApplications.get(myPlayerId).get(groupId).getFriendPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(myPlayerId);
		SimplePlayerInfo simplePlayerInfo = PbBuilder.buildSimplePlayerInfo(player);

		ChatAgreeGroupOtherServerPush_51000053.Builder pushResponse = ChatAgreeGroupOtherServerPush_51000053
				.newBuilder();
		pushResponse.setGroupId(groupId + "");
		pushResponse.setPlayerId(friendPlayerId + "");
		pushResponse.setIsAgree(isAgree);
		pushResponse.setApplyPlayer(simplePlayerInfo);

		deleteOneGroupApplication(myPlayerId, groupId);

		// 转发协议
		GameClientManager.getInstance().sendToGameServer(serverId, pushResponse.build());
			
		return OldErrorMsgEnum.ok;
	}
	
	/**
	 * 其它服务器发来的审批 是否入群处理
	 * @param myPlayerId 受邀人
	 * @param groupId 要加入的群组
	 * @param isAgree 是否同意
	 */
	public OldErrorMsgEnum otherServerApprovalGroup(long myPlayerId, long groupId, String serverId, boolean isAgree, String name, long sendPlayerId) {
		OldErrorMsgEnum errorMsgEnum = OldErrorMsgEnum.ok;
		if(isAgree) {			
			errorMsgEnum = joinOneGroup(myPlayerId, groupId, serverId, isAgree, false);						
		}
		
		sendPlayerIsAgreeMessage(name, groupId, isAgree, sendPlayerId);		
		return errorMsgEnum;
	}
	
	/**
	 * 加入一个群组
	 * @param myPlayerId
	 * @param groupId
	 * @param serverId
	 * @param isAgree
	 * @return
	 */
	private OldErrorMsgEnum joinOneGroup(long myPlayerId, long groupId, String serverId, boolean isAgree, boolean playerAdd) {
		// 校验群组是否存在
		if ((!hasGroup(groupId)) || (!hasGroupMember(groupId)))
			return OldErrorMsgEnum.not_group;

		// 校验群组是否满员
		if (groupMemberInfos.get(groupId).size() + 1 > groupMaxMemberCount)
			return OldErrorMsgEnum.max_group_count;

		joinOneGroupG(groupId, myPlayerId, serverId);
		
		// 是否写入玩家群组，跨服情况不写入，由跨服逻辑处理
		if(playerAdd)
			joinOneGroupP(myPlayerId, groupId, serverId);
		
		return OldErrorMsgEnum.ok;
	}
	
	/**
	 * 玩家点击同意加入群组 加入群
	 * 
	 * @param groupId
	 * @param myPlayerId
	 * @param serverId
	 * @return
	 */
	public void joinOneGroupG(long groupId, long myPlayerId, String serverId) {
		// 加入群组
		GroupMember groupMember = new GroupMember();
		groupMember.setGroupId(groupId);
		groupMember.setPlayerId(myPlayerId);
		groupMember.setPlayerServerId(serverId);
		groupMemberInfos.get(groupId).add(groupMember);

		ChatHelper.insertGroupMember(groupMember);		
	}

	/**
	 * 玩家点击同意加入群组 增加玩家所有群组
	 * 
	 * @param myPlayerId
	 * @param groupId
	 * @param serverId
	 * @return
	 */
	public OldErrorMsgEnum joinOneGroupP(long myPlayerId, long groupId, String serverId) {
		
		PlayerGroup playerGroup = new PlayerGroup();
		playerGroup.setGroupId(groupId);
		playerGroup.setGroupServerId(serverId);
		playerGroup.setPlayerId(myPlayerId);
		
		// 校验玩家的可加入群组数 是否重复加入群组
		if(!hasPlayerGroup(myPlayerId)) {			
			List<PlayerGroup> groupIds = new ArrayList<>();
			playerGroupInfos.put(myPlayerId, groupIds);
		}						
		playerGroupInfos.get(myPlayerId).add(playerGroup);

		// 更新db
		ChatHelper.insertPlayerGroup(playerGroup);

		return OldErrorMsgEnum.ok;
	}

	/**
	 * 删除一个玩家的一个群组的入群邀请
	 * 
	 * @param playerId
	 * @param groupId
	 */
	private void deleteOneGroupApplication(long playerId, long groupId) {

		playerGroupApplications.get(playerId).remove(groupId);

		Object[] ids = new Object[] { playerId, groupId };
		ChatHelper.deletePlayerGroupApplication(ids);
	}

	/**
	 * 发送玩家是否同意邀请入群 发给邀请者
	 * @param name 受邀人名字
	 * @param groupId 发送
	 * @param isAgree
	 * @param sendPlayerId
	 */
	private void sendPlayerIsAgreeMessage(String name, long groupId, boolean isAgree, long sendPlayerId) {
		// 发送给 发出邀请的人 已经成功入群了 或不同意入群		
		ChatAgreeGroupInvitationPush_51000060.Builder response = ChatAgreeGroupInvitationPush_51000060.newBuilder();
		response.setGroupId(groupId + "");
		response.setName(name);
		response.setIsAgree(isAgree);

		GameClientManager.getInstance().noticeOne(response.build(), sendPlayerId);
	}

	/**
	 * 检查一个玩家是否在群组里
	 * 
	 * @param groupId
	 * @param playerId
	 * @return
	 */
	public boolean checkMemberGroup(long groupId, long playerId) {		
		for (GroupMember member : groupMemberInfos.get(groupId)) {
			if (member.getPlayerId().longValue() == playerId) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 获取一个玩家的 已加入的群组数量 不算创建的
	 * @param playerId
	 * @return
	 */
	private int checkPlayerGroupCount(long playerId) {
		int count = 0;

		if (!hasPlayerGroup(playerId))
			return count;

		long groupId = 0L;
		
		for (PlayerGroup playerGroup : playerGroupInfos.get(playerId)) {
			groupId = playerGroup.getGroupId();
			if(GameServer.getInstance().isLocalServer(playerGroup.getGroupServerId())) {
				if(groupInfos.containsKey(groupId)) {
					if(groupInfos.get(groupId).getManagerId() == playerId)
						continue;
				}
			}
			
			count++;
		}
		
		return count;
	}

	/**
	 * 获取一个玩家的所有群组信息
	 * 
	 * @param playerId
	 * @return
	 */
	public List<ChatGroupInfo> getOnePlayerAllGroupInfo(long playerId) {
		List<ChatGroupInfo> allGroups = new ArrayList<>();
		if (!hasPlayerGroup(playerId))
			return allGroups;

		if (playerGroupInfos.get(playerId).size() < 1)
			return allGroups;
				
		Iterator<PlayerGroup> itemIterator = playerGroupInfos.get(playerId).iterator();
		while (itemIterator.hasNext()) {
			PlayerGroup playerGroup = itemIterator.next();
			ChatGroupInfo chatGroupInfo = getServerChatGroupInfo(playerGroup.getGroupId().toString(), playerGroup.getGroupServerId());
			if(chatGroupInfo != null) {
				allGroups.add(chatGroupInfo);				
			}else {
				// 查不到就删除玩家这个群组，说明那个群组已经解散了
				itemIterator.remove();
				
				Object[] ids = new Object[] { playerId, playerGroup.getGroupId()};
				ChatHelper.deletePlayerGroupByMap(ids);
			}
		}		

		return allGroups;
	}

	/**
	 * 获取一个群组的全部消息 包括跨服
	 * 
	 * @param groupId
	 * @param serverId
	 * @return
	 */
	public ChatGroupInfo getServerChatGroupInfo(String groupId, String serverId) {
		Long groupIdLong = Long.parseLong(groupId);
								
		if (GameServer.getInstance().isLocalServer(serverId)) {
			if (!hasGroupMember(groupIdLong))
				return null;
			
			return getLocalGroupInfo(groupIdLong);
		} else {
 			return getOtherGroupInfo(groupIdLong, serverId);
		}
	}
	
	/**
	 * 获取本服的群组信息
	 * @param groupIdLong 群组id
	 * @return
	 */
	public ChatGroupInfo getLocalGroupInfo(long groupIdLong) {
		String playerIdStr = "";
		String serverIdStr = "";
		List<SimplePlayerInfo> playerInfos = new ArrayList<>();
		ChatGroupInfo.Builder groupBuilder = ChatGroupInfo.newBuilder();
		
		int online = 0;
		for (GroupMember groupMember : groupMemberInfos.get(groupIdLong)) {
			playerIdStr = groupMember.getPlayerId().toString();
			serverIdStr = groupMember.getPlayerServerId();
			SimplePlayerInfo simplePlayerInfo = PlayerManager.getInstance().getSimpleOtherPlayerInfo(playerIdStr,
					serverIdStr);
			
			if(simplePlayerInfo == null)
				continue;
				
			playerInfos.add(simplePlayerInfo);
			
			if(simplePlayerInfo.getOnline())
				online++;
		}

		ChatGroupBriefInfo briefInfo = PbBuilder.getGroupBriefInfo(groupInfos.get(groupIdLong), online, groupMemberInfos.get(groupIdLong).size());

		groupBuilder.addAllPlayerInfos(playerInfos);
		groupBuilder.setBriefInfo(briefInfo);
		return groupBuilder.build();
	}
	
	
	/**
	 * 获取其他服的群组信息
	 * @param groupIdLong
	 * @param serverId
	 * @return
	 */
	public ChatGroupInfo getOtherGroupInfo(long groupIdLong, String serverId) {
		// 获取跨服的信息
		try {
			ChatGroupInfo chatGroupInfo = PbBuilder.buildChatGroupInfo(GameServer.getInstance().getCrossGameServerInterfaceSync()
					.getChatGroupInfo(groupIdLong, serverId)); 
						
			if (chatGroupInfo != null)
				return chatGroupInfo;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获取一个群组的简单消息 包括跨服
	 * 
	 * @param groupId
	 * @param serverId
	 * @return
	 */
	public ChatGroupBriefInfo getServerGroupBriefInfo(String groupId, String serverId) {
		Long groupKey = Long.parseLong(groupId);
		
		if (!serverId.equals(ServerContext.getInstance().getServerId())) {
			// 跨服查群组信息 先return null
			try {
 				 Group group = GameServer.getInstance().getCrossGameServerInterfaceSync()
						.getOneGroupBriefInfo(Long.parseLong(groupId), serverId);
 				 
				 ChatGroupBriefInfo groupBriefInfo = PbBuilder.getGroupBriefInfo(group);
				 				
				if (groupBriefInfo != null)
					return groupBriefInfo;

			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			return null;
		}
		
		if (!hasGroup(groupKey))
			return null;

		return PbBuilder.getGroupBriefInfo(groupInfos.get(groupKey), groupMemberInfos.get(groupKey));
	}

	/**
	 * 获取审批列表的所有信息
	 * 
	 * @param playerId
	 */
	public List<ChatGroupInviteInfo> getPlayerInviteInfos(long playerId) {

		List<ChatGroupInviteInfo> inviteInfos = new ArrayList<>();
		if (!hasGroupApplications(playerId))
			return inviteInfos;

		long groupId = 0;

		for (long key : playerGroupApplications.get(playerId).keySet()) {
			
			try {
				PlayerGroupApplication application = playerGroupApplications.get(playerId).get(key);

				ChatGroupInviteInfo.Builder chatGroupInviteInfo = ChatGroupInviteInfo.newBuilder();
				groupId = application.getGroupId();
				
				if (GameServer.getInstance().isLocalServer(application.getServerId()) ) {
					if (hasGroup(groupId)) {
						chatGroupInviteInfo.setGroupBriefInfo(PbBuilder.getGroupBriefInfo(groupInfos.get(groupId), groupMemberInfos.get(groupId)));					 
						
						SimplePlayer simplePlayer = PlayerManager.getInstance().getAndLoadSimplePlayer(application.getPlayerId());
						chatGroupInviteInfo.setName(simplePlayer.getName());
																
						chatGroupInviteInfo.setServerId(application.getServerId());
						inviteInfos.add(chatGroupInviteInfo.build());
					} 
					else {
						deleteOneGroupApplication(playerId, groupId);
					}
					
				}else {	
									 													
					Group group = GameServer.getInstance().getCrossGameServerInterfaceSync()
								.getOneGroupBriefInfo(groupId, application.getServerId());
					ChatGroupBriefInfo groupBriefInfo = PbBuilder.getGroupBriefInfo(group);
						
					if (groupBriefInfo == null) {
						deleteOneGroupApplication(playerId, groupId);
						continue;
					}

					chatGroupInviteInfo.setGroupBriefInfo(groupBriefInfo);

					SimplePlayer simplePlayer = PlayerManager.getInstance()
							.getAndLoadSimplePlayer(application.getFriendPlayerId(), application.getServerId());
					chatGroupInviteInfo.setName(simplePlayer.getName());

					chatGroupInviteInfo.setServerId(application.getServerId());
					inviteInfos.add(chatGroupInviteInfo.build());																			
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
												
		}
		return inviteInfos;
	}

	/**
	 * 遍历玩家全部群组信息 返回
	 * 
	 * @param groupId
	 * @return
	 */
	private ChatGroupInfo getGroupInfo(long groupId) {
		try {
			ChatGroupInfo.Builder groupBuilder = ChatGroupInfo.newBuilder();
			ChatGroupBriefInfo briefInfo = PbBuilder.getGroupBriefInfo(groupInfos.get(groupId), groupMemberInfos.get(groupId));

			List<SimplePlayerInfo> simplePlayerInfos = new ArrayList<>();
			for (GroupMember groupMember : groupMemberInfos.get(groupId)) {
				SimplePlayer simplePlayer = PlayerManager.getInstance()
						.getAndLoadSimplePlayer(groupMember.getPlayerId(), groupMember.getPlayerServerId());
				SimplePlayerInfo simplePlayerInfo = PbBuilder.buildSimplePlayerInfo(simplePlayer);
				simplePlayerInfos.add(simplePlayerInfo);
			}

			groupBuilder.setBriefInfo(briefInfo);
			groupBuilder.addAllPlayerInfos(simplePlayerInfos);
			return groupBuilder.build();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 根据群组id获取一个群组的简略信息 跨服调用也可用
	 * 
	 * @param groupId
	 * @return
	 */
	public ChatGroupBriefInfo getGroupBriefInfoById(long groupId) {
		if (!hasGroup(groupId))
			return null;
		
		if (!hasGroupMember(groupId))
			return null;

		return PbBuilder.getGroupBriefInfo(groupInfos.get(groupId), groupMemberInfos.get(groupId));
	}
	
	public Group getGroupBriefInfoByIdG(long groupId) {
		if (!hasGroup(groupId))
			return null;

		return PbBuilder.getGroup(groupInfos.get(groupId), groupMemberInfos.get(groupId));
	}

	/**
	 * 根据群组id获取一个群组的全部信息 跨服调用也可用，不能在主线程调用
	 * 
	 * @param groupId
	 * @return
	 */
	public ChatGroupInfo getChatGroupInfo(long groupId) {
		if (!hasGroup(groupId))
			return null;

		return getGroupInfo(groupId);
	}

	/**
	 * 检查一个玩家对群组的权限，内置判断群组存在和玩家存在是否
	 * 
	 * @param groupId
	 * @param playerId
	 * @return
	 */
	private boolean checkGroupJurisdiction(long groupId, long playerId) {
		if (!hasGroup(groupId))
			return false;

		if (groupInfos.get(groupId).getManagerId().longValue() != playerId)
			return false;

		if (!hasPlayerGroup(playerId))
			return false;

		for (PlayerGroup playerGroup : playerGroupInfos.get(playerId)) {
			if (playerGroup.getGroupId().longValue() == groupId) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 修改一个群组的名字
	 * 
	 * @param groupId
	 * @param client
	 * @param newName
	 */
	public OldErrorMsgEnum setGroupName(long groupId, long myPlayerId, String newName) {
		if (!checkGroupJurisdiction(groupId, myPlayerId)) {
			return OldErrorMsgEnum.not_jurisdiction;
		}

		if (Config.checkKeyWord(newName)) {
			return OldErrorMsgEnum.not_name;
		}

		groupInfos.get(groupId).setName(newName);
		ChatHelper.updateGroup(groupInfos.get(groupId));

		return OldErrorMsgEnum.ok;
	}

	/**
	 * 修改群组的头像
	 * 
	 * @param groupId
	 * @param client
	 * @param newHeadId
	 * @param serverId
	 */
	public boolean setGroupHeadId(long groupId, long playerId, int newHeadId, String serverId) {
		if (!checkGroupJurisdiction(groupId, playerId)) {
			return false;
		}

		groupInfos.get(groupId).setHeadIcon(newHeadId);
		ChatHelper.updateGroup(groupInfos.get(groupId));

		return true;
	}

	/**
	 * 修改群组公告
	 * 
	 * @param groupId
	 * @param client
	 * @param notice
	 * @param serverId
	 */
	public OldErrorMsgEnum setGroupNotice(Long groupId, long myPlayerId, String notice) {

		if (!checkGroupJurisdiction(groupId, myPlayerId)) {
			return OldErrorMsgEnum.not_jurisdiction;
		}

		if (Config.checkKeyWord(notice)) {
			return OldErrorMsgEnum.not_notice;
		}

		groupInfos.get(groupId).setNotice(notice);
		ChatHelper.updateGroup(groupInfos.get(groupId));

		return OldErrorMsgEnum.ok;
	}
}
