package cn.game.games.net.game.module.mail;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.games.cache.entity.Mail;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.handler.GameBaseHandler;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.util.PbBuilder;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.MailMsg.MailDeleteRequest_12000007;
import cn.game.protocol.protobuf.MailMsg.MailDeleteResponse_12000008;
import cn.game.protocol.protobuf.MailMsg.MailListResponse_12000002;
import cn.game.protocol.protobuf.MailMsg.MailListRequest_12000001;
import cn.game.protocol.protobuf.MailMsg.MailReceiveRequest_12000005;
import cn.game.protocol.protobuf.MailMsg.MailReceiveResponse_12000006;
import cn.game.protocol.protobuf.MailMsg.MailSeeRequest_12000003;
import cn.game.protocol.protobuf.MailMsg.MailSeeResponse_12000004;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/** 邮件协议边界；列表在缓存重放前鉴权并保持只读，既有写操作继续沿用统一写门禁。 */
@Component
public class MailHandler extends GameBaseHandler {
	/**
	 * 在needProcess可能重放响应缓存之前核对当前连接，不能让失效连接借旧序号绕过鉴权。
	 * @param client 本次原连接
	 * @param protocol 正式请求
	 */
	@Override
	public void dispatch(NetClient client, cn.game.core.net.protocol.IProtocol<?> protocol) {
		if (protocol.getMsgID() == PbProtocol.MailListRequest_12000001 && !checkExt(client, protocol)) { return; }
		super.dispatch(client, protocol);
	}

	/**
	 * 列表仅读取当前有效连接的玩家，不进入聚合写作用域；保留退出时拒绝规则。
	 * @param client 当前请求连接
	 * @param protocol 已解码的请求
	 * @return 是否允许实际处理
	 */
	@Override
	public boolean checkExt(NetClient client, cn.game.core.net.protocol.IProtocol<?> protocol) {
		if (protocol.getMsgID() != PbProtocol.MailListRequest_12000001) { return super.checkExt(client, protocol); }
		Player player = client.getPlayerId() > 0 ? findPlayer(client.getPlayerId()) : null;
		if (!(client instanceof cn.game.games.net.client.GameClient gameClient)
				|| player == null || player.getGameClient() != client
				|| gameClient.state != cn.game.games.net.client.GameClient.GameClientState.IN_GAME) {
			rejectList(client, protocol, ErrorMsgEnum.need_login);
			return false;
		}
		if (player.isIslogouting()) {
			rejectList(client, protocol, ErrorMsgEnum.player_logouting);
			return false;
		}
		return true;
	}

	/**
	 * 鉴权可能发生在needProcess设置当前序号之前，必须绑定原请求且不污染成功响应缓存。
	 * @param client 原连接
	 * @param protocol 本次原请求
	 * @param error 既有拒绝错误码
	 */
	private void rejectList(NetClient client, cn.game.core.net.protocol.IProtocol<?> protocol, ErrorMsgEnum error) {
		var response = cn.game.protocol.protobuf.PlayerMsg.PlayerErrorPush_01000099.getDefaultInstance();
		if (client instanceof cn.game.games.net.client.GameClient gameClient) {
			gameClient.sendProtocol(PbProtocol.PlayerErrorPush_01000099, protocol.getSeq(), response.toByteArray(), error.getId(), false);
		} else { client.sendProtocol(response, error.getId()); }
	}
	@Override
	protected int getModule() {
		return 0x12;
	}

	@Override
	protected void inititialize() {

		putInvoker(PbProtocol.MailListRequest_12000001, this::list);
		putInvoker(PbProtocol.MailSeeRequest_12000003, this::see);
		putInvoker(PbProtocol.MailReceiveRequest_12000005, this::receive);
		putInvoker(PbProtocol.MailDeleteRequest_12000007, this::delete);
	}

	private void see(NetClient client, Object message) {
		MailSeeRequest_12000003 req = (MailSeeRequest_12000003) message;
		MailSeeResponse_12000004.Builder resp = MailSeeResponse_12000004.newBuilder();

		String uid = req.getUid();
		long id = StringUtils.isEmpty(uid) ? 0 : Long.parseLong(uid);
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());

		MailModule mailModule = player.getMailModule();

		if (id > 0) {
			Mail mail = mailModule.get(id);
			if (mail == null) {
				client.sendProtocol(resp, ErrorMsgEnum.player_data_not_found.getId());
				return;
			}
			mailModule.see(id);
		} else {
			mailModule.seeBatch();
		}
		client.sendProtocol(resp.build());
	}

	private void receive(NetClient client, Object message) {
		MailReceiveRequest_12000005 req = (MailReceiveRequest_12000005) message;
		MailReceiveResponse_12000006.Builder resp = MailReceiveResponse_12000006.newBuilder();

		String uid = req.getUid();
		long id = StringUtils.isEmpty(uid) ? 0 : Long.parseLong(uid);
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		MailModule mailModule = player.getMailModule();
		List<RewardInfo> ret = null;
		if (id > 0) {
			ret = mailModule.receive(id);
		} else {
			ret = mailModule.receiveBatch();
		}
		resp.addAllRewards(ret);
		client.sendProtocol(resp.build());
	}

	private void delete(NetClient client, Object message) {
		MailDeleteRequest_12000007 req = (MailDeleteRequest_12000007) message;
		String uid = req.getUid();
		int type = req.getType(); 
		long id = StringUtils.isEmpty(uid) ? 0 : Long.parseLong(uid);
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		MailModule mailModule = player.getMailModule();
		if (id > 0) {
			mailModule.delete(id);
		} else {
			if (type > 0) {
				mailModule.deleteByType(type);
			}else {
				mailModule.deleteAll();
			}
		}
		client.sendProtocol(MailDeleteResponse_12000008.getDefaultInstance());
	}

	private void list(NetClient client, Object message) {
		MailListRequest_12000001 req = (MailListRequest_12000001) message;
		MailListResponse_12000002.Builder resp = MailListResponse_12000002.newBuilder();
		Player player = findPlayer(client.getPlayerId());
		MailModule mailModule = player.getMailModule();
		try {
			// 空请求的全量语义不可截断；显式ALL与未设置筛选通过presence区分。
			MailPage.Result page = !req.hasFilter() && !req.hasPageSize() && req.getCursor().isEmpty()
					? new MailPage.Result(mailModule.legacyList(), "")
					: mailModule.page(req.getFilterValue(), req.hasPageSize() ? req.getPageSize() : MailPage.DEFAULT_SIZE, req.getCursor());
			resp.addAllMails(PbBuilder.buildAllMailInfo(page.mails()));
			resp.setNextCursor(page.nextCursor());
		} catch (cn.game.core.exception.LogicException failure) {
			log.warn("邮件列表参数拒绝 playerId={} reason={}", player.getPlayerId(), failure.getErrorMessage(), failure);
			client.sendProtocol(cn.game.protocol.protobuf.PlayerMsg.PlayerErrorPush_01000099.newBuilder()
					.setError(failure.getErrorMessage()).build(), failure.getErrorCode());
			return;
		}

		client.sendProtocol(resp.build());
	}

}
