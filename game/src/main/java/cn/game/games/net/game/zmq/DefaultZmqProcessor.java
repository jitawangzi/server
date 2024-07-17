package cn.game.games.net.game.zmq;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.socket.controller.Dispatcher;
import cn.game.core.task.TaskManager;
import cn.game.util.MailUtil;
import cn.game.util.SpringContextLoader;

/**
 * @Description 处理zmq消息
 * 2017年4月1日 上午11:35:47
 * @author SYQ
 */
public class DefaultZmqProcessor implements ZmqProcessor {

	private static Logger log = LoggerFactory.getLogger("usetimeLog");
	private Dispatcher dispatcher;

	public DefaultZmqProcessor(ZContext context) {
		dispatcher = SpringContextLoader.getContext().getBean(Dispatcher.class);
	}

	@Override
	public void process(final NetClient netClient, final IProtocol protocol) {

		TaskManager.getInstance().addMainTask(() -> {

			long start = System.currentTimeMillis();
			try {
				dispatcher.dispatch(netClient, protocol);
			} catch (Throwable e) {
				e.printStackTrace();
				log.error(netClient + "run msg" + "0x" + Integer.toHexString(protocol.getMsgID()) + "err", e);

				try {
					MailUtil.reportException("玩家:" + netClient + "请求处理异常", ExceptionUtils.getFullStackTrace(e));
				} catch (Exception e1) {
					e1.printStackTrace();
				}

			}
			if (log.isDebugEnabled()) {
				log.debug("{} run msg[{}] use time[{}]ms", netClient, "0x" + Integer.toHexString(protocol.getMsgID()),
						System.currentTimeMillis() - start);
			}

		});
	}

}
