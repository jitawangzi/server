package cn.game.games.net.cross.guild;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.core.base.ServerContext;
import cn.game.core.exception.LogicException;
import cn.game.core.net.client.NetClient;
import cn.game.games.net.cross.guild.dto.MemberAuthRequest;
import cn.game.games.net.cross.guild.dto.GuildSettingRequest;
import cn.game.games.net.cross.guild.service.GuildService;
import cn.game.games.net.cross.guild.service.GuildServiceInterface;
import cn.game.games.net.game.handler.GameBaseHandler;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.GuildCrossMsg;
import cn.game.protocol.protobuf.GuildMsg;
import cn.game.protocol.protobuf.GuildMsg.GuildAllInfo;

/**
 * @ClassName GuildHandler
 *
 * @description: 公会请求处理器 - 负责协议解析和调用公会服务
 * @author: ly
 * @create: 2025-02-06 15:11 @Version 1.0
 */
@Component
public class GuildCrossHandler extends GameBaseHandler {

	private final GuildServiceInterface guildService = GuildService.getInstance();

	@Override
	protected void inititialize() {
		putInvoker(PbProtocol.GuildMsgPush_41000045, this::dispatchMsg);
	}

	@Override
	protected int getModule() {
		return 0x41;
	}

	private void dispatchMsg(NetClient client, Object o) {
		GuildCrossMsg.GuildMsgPush_41000045 request = (GuildCrossMsg.GuildMsgPush_41000045) o;
		int msgId = request.getMsgId();
		long playerId = request.getPlayerId();
		long guildId = request.getGuildId();
		List<String> paramList = new ArrayList<>(request.getParamsList().stream().toList());
		Message message = PbProtocol.getInstance().parseFrom(msgId, request.getData());

		GuildManager.log.info(String.format("dispatchMsg pid:%s guildId:%s cmd:%s req:%s", playerId, guildId,
				message.getClass().getSimpleName(), message));

		ServerContext.getInstance().getProcessor().process(guildId, () -> {
			try {
				switch (msgId) {
				case PbProtocol.GuildInfoRequest_40000021 -> getGuildInfo(guildId, playerId, message, paramList, client);
				case PbProtocol.GuildCreateRequest_40000005 -> createGuild(playerId, message, paramList, client);
				case PbProtocol.GuildApplyJoinRequest_40000007 -> applyJoinGuild(playerId, message, paramList, client);
				case PbProtocol.GuildDissolveRequest_40000011 -> dissolveGuild(guildId, playerId, message, paramList, client);
				case PbProtocol.GuildSettingRequest_40000013 -> setGuildSetting(guildId, playerId, message, paramList, client);
				case PbProtocol.GuildLogRequest_40000025 -> getGuildLog(guildId, playerId, message, paramList, client);
				case PbProtocol.GuildMemberPositionSetRequest_40000015 ->
					setGuildMemberPosition(guildId, playerId, message, paramList, client);
				case PbProtocol.GuildFindRequest_40000003 -> findGuild(guildId, playerId, message, paramList, client);
				case PbProtocol.ChatMessagePush_31010001 -> guildChat(guildId, playerId, message, paramList, client);
				default -> {
					GuildManager.log.error("Unknown msgId: " + msgId + " for playerId: " + playerId);
					sendErrorCodeMsgToGameServer(playerId, client, ErrorMsgEnum.unknown.ID, msgId + 1);
				}
				}

			} catch (LogicException e) {
				sendErrorCodeMsgToGameServer(playerId, client, e.getErrorCode(), msgId + 1);
			} catch (Exception e) {
				sendErrorCodeMsgToGameServer(playerId, client, ErrorMsgEnum.unknown.ID, msgId + 1);
				e.printStackTrace();
			}

		});
	}

	// 获取公会信息
	private void getGuildInfo(long guildId, long playerId, Message message, List<String> paramList, NetClient client) {
//		Guild info = guildService.getGuild(guildId);
//		GuildMsg.GuildServiceInfo infoProto = info.toProto();
//		sendMsgToGameServer(playerId, client, GuildMsg.GuildInfoResponse_40000022.newBuilder().setInfo(infoProto).build(),
//				PbProtocol.GuildInfoResponse_40000022);
	}

	// 创建公会
	private void createGuild(long playerId, Message message, List<String> paramList, NetClient client) {
		GuildMsg.GuildCreateRequest_40000005 req = (GuildMsg.GuildCreateRequest_40000005) message;

//		guildService.createGuild(req, playerId).map(guildInfo -> {
//			GuildMsg.GuildCreateResponse_40000006.Builder res = GuildMsg.GuildCreateResponse_40000006.newBuilder();
//			res.setGuild(guildInfo.toProto());
//			sendMsgToGameServer(playerId, client, res.build(), PbProtocol.GuildCreateResponse_40000006);
//			return null ; 
//		}).onFailure(err -> {
//			if (err instanceof LogicException le) {
//				sendErrorCodeMsgToGameServer(playerId, client, le.getErrorCode(), PbProtocol.GuildCreateResponse_40000006);
//			} else {
//				log.error("create guild fail", err);
//				sendErrorCodeMsgToGameServer(playerId, client, ErrorMsgEnum.unknown.ID, PbProtocol.GuildCreateResponse_40000006);
//			}
//		});
	}

	// 申请加入公会
	private void applyJoinGuild(long playerId, Message message, List<String> paramList, NetClient client) {
		GuildMsg.GuildApplyJoinRequest_40000007 req = (GuildMsg.GuildApplyJoinRequest_40000007) message;
//		long guildId = req.getId();
//		int power = Integer.parseInt(paramList.get(0));
//		String playerName = paramList.get(1);
//
//		GuildAllInfo info = guildService.applyJoinGuild(guildId, playerId);
//		GuildMsg.GuildApplyJoinResponse_40000008.Builder res = GuildMsg.GuildApplyJoinResponse_40000008.newBuilder();
//		if (info != null) {
//			res.setGuild(info);
//		}
//		sendMsgToGameServer(playerId, client, res.build(), PbProtocol.GuildApplyJoinResponse_40000008);
	}

	// 解散公会
	private void dissolveGuild(long guildId, long playerId, Message message, List<String> paramList, NetClient client) {
		guildService.dissolveGuild(guildId, playerId);
		sendMsgToGameServer(playerId, client, GuildMsg.GuildDissolveResponse_40000012.newBuilder().build(),
				PbProtocol.GuildDissolveResponse_40000012);
	}

	// 设置公会配置
	private void setGuildSetting(long guildId, long playerId, Message message, List<String> paramList, NetClient client) {
		GuildMsg.GuildSettingRequest_40000013 req = (GuildMsg.GuildSettingRequest_40000013) message;

		GuildSettingRequest settingRequest = new GuildSettingRequest();
		settingRequest.setOperatorId(playerId);
		settingRequest.setOperatorName(paramList.get(0));
		settingRequest.setName(req.getName());
		settingRequest.setWx(req.getWx());
		settingRequest.setNotice(req.getNotice());
		settingRequest.setDeclaration(req.getDeclaration());
		settingRequest.setIcon(req.getIcon());
		settingRequest.setAutoJoin(req.getAutoJoin());

		boolean setGuildSetting = guildService.setGuildSetting(guildId, settingRequest); 
		if (setGuildSetting) {

			sendMsgToGameServer(playerId, client, GuildMsg.GuildSettingResponse_40000014.newBuilder().setResult(true).build(),
					PbProtocol.GuildSettingResponse_40000014);
		}else {
			sendErrorCodeMsgToGameServer(playerId, client, ErrorMsgEnum.unknown.ID, PbProtocol.GuildSettingResponse_40000014);
		}
	}

	// 获取公会日志
	private void getGuildLog(long guildId, long playerId, Message message, List<String> paramList, NetClient client) {
		Guild info = guildService.getGuild(guildId);
		GuildMsg.GuildLogResponse_40000026.Builder res = GuildMsg.GuildLogResponse_40000026.newBuilder();
		res.addAllLogList(info.getModule().optLog.toProto());
		sendMsgToGameServer(playerId, client, res.build(), PbProtocol.GuildLogResponse_40000026);
	}

	// 设置成员职位
	private void setGuildMemberPosition(long guildId, long playerId, Message message, List<String> paramList, NetClient client) {
		GuildMsg.GuildMemberPositionSetRequest_40000015 req = (GuildMsg.GuildMemberPositionSetRequest_40000015) message;
		GuildMember member = guildService.getMember(guildId, req.getTargetPid()); 
		int oldPosition = member == null ? 0 :  member.getPosition();
		guildService.setMemberPosition(guildId, playerId, req.getTargetPid(), req.getPosition());
		sendMsgToGameServer(playerId, client,
				GuildMsg.GuildMemberPositionSetResponse_40000016.newBuilder()
						.setResult(true)
						.setPosition(req.getPosition())
						.setOldPosition(oldPosition)
						.setTargetPid(req.getTargetPid())
						.build(),
				PbProtocol.GuildMemberPositionSetResponse_40000016);
	}

	// 查找公会
	private void findGuild(long guildId, long playerId, Message message, List<String> paramList, NetClient client) {
		Guild info = guildService.getGuild(guildId);
		GuildMsg.GuildFindResponse_40000004.Builder res = GuildMsg.GuildFindResponse_40000004.newBuilder();
		res.setGuild(info.toShowProto());
		sendMsgToGameServer(playerId, client, res.build(), PbProtocol.GuildFindResponse_40000004);
	}

	// 公会聊天
	private void guildChat(long guildId, long playerId, Message message, List<String> paramList, NetClient client) {
		Guild info = guildService.getGuild(guildId);
		List<Long> memberIdList = new ArrayList<>(info.getModule().menMemberMap.keySet());
		GuildHelper.broadcastNotifyMsgToPlayer(message, PbProtocol.ChatMessagePush_31010001, memberIdList);
	}

	public void sendErrorCodeMsgToGameServer(long playerId, NetClient client, int errorCode, int msgId) {
		GuildCrossMsg.GuildMsgResponse_41000046.Builder response = GuildCrossMsg.GuildMsgResponse_41000046.newBuilder();
		response.setMsgId(msgId);
		response.setPlayerId(playerId);
		response.setErrorCode(errorCode);
		client.sendProtocol(response.build());
		GuildManager.log.error(String.format("%d %d %d", errorCode, playerId, msgId));
	}

	public void sendMsgToGameServer(long playerId, NetClient client, Message message, int msgId) {
		GuildCrossMsg.GuildMsgResponse_41000046.Builder response = GuildCrossMsg.GuildMsgResponse_41000046.newBuilder();
		response.setMsgId(msgId);
		response.setErrorCode(ErrorMsgEnum.ok.getId());
		response.setPlayerId(playerId);
		response.setData(message.toByteString());
		client.sendProtocol(response.build());
		GuildManager.log.info(String.format("pid:%d msgId:%d message:%s", playerId, msgId, message));
	}
}