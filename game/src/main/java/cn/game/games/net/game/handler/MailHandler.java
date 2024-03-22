package cn.game.games.net.game.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.base.PlayerCacheFactory;
import cn.game.games.cache.entity.Mail;
import cn.game.games.cache.op.impl.MailOp;
import cn.game.games.util.PbBuilder;
import cn.game.protocol.manual.OldErrorMsgEnum;
import cn.game.protocol.protobuf.MailMsg.MailDeleteRequest_12000007;
import cn.game.protocol.protobuf.MailMsg.MailDeleteResponse_12000008;
import cn.game.protocol.protobuf.MailMsg.MailListResponse_12000002;
import cn.game.protocol.protobuf.MailMsg.MailReceiveRequest_12000005;
import cn.game.protocol.protobuf.MailMsg.MailReceiveResponse_12000006;
import cn.game.protocol.protobuf.MailMsg.MailSeeRequest_12000003;
import cn.game.protocol.protobuf.MailMsg.MailSeeResponse_12000004;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

@Component
public class MailHandler extends BaseHandler {
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
		MailOp mailOp = PlayerCacheFactory.getCache(client.getPlayerId(), MailOp.class);

		if (id > 0) {
			Mail mail = mailOp.get(id);
			if (mail == null) {
				client.sendProtocol(resp, OldErrorMsgEnum.player_data_not_found.getId());
				return;
			}
			mailOp.see(id);
		} else {
			mailOp.seeBatch();
		}
		client.sendProtocol(resp.build());
	}

	private void receive(NetClient client, Object message) {
		MailReceiveRequest_12000005 req = (MailReceiveRequest_12000005) message;
		MailReceiveResponse_12000006.Builder resp = MailReceiveResponse_12000006.newBuilder();

		String uid = req.getUid();
		long id = StringUtils.isEmpty(uid) ? 0 : Long.parseLong(uid);
		MailOp mailOp = PlayerCacheFactory.getCache(client.getPlayerId(), MailOp.class);
		List<RewardInfo> ret = null;
		if (id > 0) {
			ret = mailOp.receive(id);
		} else {
			ret = mailOp.receiveBatch();
		}
		resp.addAllRewards(ret);
		client.sendProtocol(resp.build());
	}

	private void delete(NetClient client, Object message) {
		MailDeleteRequest_12000007 req = (MailDeleteRequest_12000007) message;
		String uid = req.getUid();
		long id = StringUtils.isEmpty(uid) ? 0 : Long.parseLong(uid);
		MailOp mailOp = PlayerCacheFactory.getCache(client.getPlayerId(), MailOp.class);

		if (id > 0) {
			Mail mail = mailOp.get(id);
			if (mail != null && mail.getSee()) {
				mailOp.delete(id);
			}
		} else {
			List<Mail> list = new ArrayList<Mail>(mailOp.list());
			for (Mail mail : list) {
				if (mail.getSee()) {
					mailOp.delete(mail.getId());
				}
			}
		}
		client.sendProtocol(MailDeleteResponse_12000008.getDefaultInstance());
	}

	private void list(NetClient client, Object message) {

		MailListResponse_12000002.Builder resp = MailListResponse_12000002.newBuilder();
		MailOp mailOp = PlayerCacheFactory.getCache(client.getPlayerId(), MailOp.class);
		Collection<Mail> list = mailOp.list();
		resp.addAllMails(PbBuilder.buildAllMailInfo(list));

		client.sendProtocol(resp.build());
	}

}
