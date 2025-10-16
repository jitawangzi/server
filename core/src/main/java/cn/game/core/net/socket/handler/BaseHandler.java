package cn.game.core.net.socket.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.exception.ExceptionUtils;

//import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cn.game.core.exception.LogicException;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.socket.controller.Dispatcher;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.PlayerMsg.PlayerErrorPush_01000099;
import cn.game.util.GameUtil;
import cn.game.util.HexUtil;

public abstract class BaseHandler implements Handler {

	protected Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	protected Dispatcher dispatcher;

	protected PbProtocol protocolParser = PbProtocol.getInstance();

	protected final Map<Integer, Invoker> CMD_INVOKERS = new HashMap<Integer, Invoker>();

	protected abstract void inititialize();

	protected abstract int getModule();

	/** 
	 * 是否应该处理当前协议的 额外的一些检查
	 * @param client
	 * @param protocol
	 * @return
	 */
	public boolean checkExt(NetClient client, IProtocol<?> protocol) {
		return true;
	}

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
					if (client.needProcess(protocol) && this.checkExt(client, protocol)) {
						invoker.invoke(client, message);
						client.afterProcess(protocol);
					}
				} catch (LogicException e) {
					client.sendProtocol(PlayerErrorPush_01000099.getDefaultInstance(), e.getErrorCode());
				} catch (Throwable e) {
					int errorCode = ErrorMsgEnum.unknown.getId() ; 
					LogicException cause = GameUtil.findCause(e,LogicException.class);
					if (cause!=null) {
						errorCode = cause.getErrorCode(); 
					}
					log.error(client + "run msg:" + HexUtil.toHexString(protocol.getMsgID()) + " err" + "data:" + protocol.getData(), e);
					client.sendProtocol(PlayerErrorPush_01000099.newBuilder()
							.setError(e.getMessage() != null ? e.getMessage() : ExceptionUtils.getFullStackTrace(e))
							.build(), errorCode);
//					CompletableFuture.runAsync(() -> {
//						try {
//							MailUtil.reportException("玩家:" + client + "请求处理异常", ExceptionUtils.getFullStackTrace(e));
//						} catch (Exception e1) {
//							e1.printStackTrace();
//						}
//					});
				}
			} else {
				this.log.warn(String.format("No Invoker for cmd:[%d]", cmd));
			}
		}
	}

	public void putInvoker(int cmd, Invoker invoker) {
		if (this.CMD_INVOKERS.containsKey(cmd)) {
			throw new IllegalArgumentException(String.format("Error: cmd[%s] duplicated key[%s] ",
					new Object[] { Integer.toHexString(getModule()), HexUtil.toHexString(cmd) }));
		}

		this.CMD_INVOKERS.put(Integer.valueOf(cmd), invoker);
	}

}
