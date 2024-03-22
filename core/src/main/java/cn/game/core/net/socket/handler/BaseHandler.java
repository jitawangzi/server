package cn.game.core.net.socket.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.exception.ExceptionUtils;

//import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.TextFormat;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.socket.controller.Dispatcher;
import cn.game.protocol.manual.OldErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.PlayerMsg.PlayerErrorPush_01000099;
import cn.game.util.Config;
import cn.game.util.HexUtil;
import cn.game.util.MailUtil;

public abstract class BaseHandler implements Handler {

	protected Logger log = LoggerFactory.getLogger(this.getClass());
	protected Logger gamerecvLog = LoggerFactory.getLogger("gamerecvLog");
	@Autowired
	protected Dispatcher dispatcher;

	protected PbProtocol protocolParser = PbProtocol.getInstance();

	protected final Map<Integer, Invoker> CMD_INVOKERS = new HashMap<Integer, Invoker>();

	protected abstract void inititialize();

	protected abstract int getModule();

	@PostConstruct
	public final void init() {
		this.dispatcher.put(getModule(), this);
		inititialize();
	}
	@Override
	public void dispatch(NetClient client, IProtocol<?> protocol) {
		int cmd = protocol.getMsgID();
		int seq = protocol.getSeq();
		if (protocol != null) {

			Invoker invoker = this.CMD_INVOKERS.get(cmd);
			if (invoker != null) {

				try {
					Object message = protocol.getData();
//					if (protocol instanceof ProtobufProtocol) {
//
//						ProtobufProtocol protobufProtocol = (ProtobufProtocol) protocol;
//						message = protobufProtocol.getData() == null ? protocolParser.parseFrom(cmd, protobufProtocol.getData())
//								: protobufProtocol.getData();
//
//					} else if (protocol instanceof RocketMqProtocol) {
//						message = protocol.getData();
//					} else if (protocol instanceof DefaultJsonProtocol) {
//						message = protocol.getData();
//					} else {
//						throw new IllegalArgumentException("no support protocol " + protocol.getClass().getSimpleName());
//					}
					if (Config.recordRecvData) {
						if (cmd != PbProtocol.PlayerHeartbeatRequest_01000005 && cmd != PbProtocol.TestExploreMapRequest_6f000035) {
							this.gamerecvLog.info("opType[recv]{}receive msg[{}]data[{}]seq[{}]", client, message.getClass()
									.getSimpleName(),
									message instanceof MessageOrBuilder ? TextFormat.shortDebugString((MessageOrBuilder) message)
											: message, seq);
						}
					}
					if (client.needProcess(protocol)) {
						invoker.invoke(client, message);
						client.afterProcess(protocol);
					}

				} catch (Throwable e) {
					log.error(client + "run msg:" + HexUtil.toHexString(protocol.getMsgID()) + " err" + "data:" + protocol.getData(),
							e);
					client.sendProtocol(PlayerErrorPush_01000099.newBuilder().setError(e.getMessage() != null ? e.getMessage()
							: ExceptionUtils.getFullStackTrace(e)).build(),
							OldErrorMsgEnum.unknown.getId());

					CompletableFuture.runAsync(() -> {
						try {
							MailUtil.reportException("玩家:" + client + "请求处理异常", ExceptionUtils.getFullStackTrace(e));
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}) ; 
				}
			} else {
				this.log.warn(String.format("No Invoker for cmd:[%d]", cmd));
			}
		}
	}

	public void putInvoker(int cmd, Invoker invoker) {
		if (this.CMD_INVOKERS.containsKey(cmd)) {
			throw new IllegalArgumentException(String.format("Error: cmd[%d] duplicated key[%d] ",
					new Object[] { Integer.toHexString(getModule()), HexUtil.toHexString(cmd) }));
		}

		this.CMD_INVOKERS.put(Integer.valueOf(cmd), invoker);
	}

}
