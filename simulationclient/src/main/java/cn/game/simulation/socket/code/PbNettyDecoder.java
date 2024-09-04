package cn.game.simulation.socket.code;
// package socket.base.code;
//
// import java.lang.reflect.Method;
// import java.util.concurrent.atomic.AtomicInteger;
// import java.util.concurrent.atomic.AtomicLong;
//
// import org.apache.mina.core.buffer.IoBuffer;
// import org.apache.mina.core.session.IoSession;
// import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
// import org.apache.mina.filter.codec.ProtocolDecoderOutput;
//
// import com.kodgames.corgi.core.net.Connection;
// import com.kodgames.corgi.core.net.handler.message.BaseMessageHandler;
// import com.kodgames.corgi.core.session.ConnectionManager;
//
// import client.ClientPoker;
// import cn.game.util.ByteHelp;
//
//
// public class PbNettyDecoder extends CumulativeProtocolDecoder
// {
//
// private static final String DECODER_STATE_KEY =
// PbNettyDecoder.class.getName() + ".STATE";
// private static final int HEADER_SIZE = 4;
// private static AtomicInteger cid = new AtomicInteger(1) ;
//
// public PbNettyDecoder()
// {
// }
//
// private class DecoderState
// {
// public int packetlength = -1;
// }
//
// @Override
// protected boolean doDecode(IoSession session, IoBuffer in,
// ProtocolDecoderOutput out) throws Exception
// {
//
// // //messageType(1)+protocolID(4)+callback(4)
// DecoderState decoderState = (DecoderState)
// session.getAttribute(DECODER_STATE_KEY);
// if (decoderState == null)
// {
// decoderState = new DecoderState();
// session.setAttribute(DECODER_STATE_KEY, decoderState);
// }
//
// if (in.remaining() >= HEADER_SIZE && decoderState.packetlength == -1)
// {
// int s = in.getInt() ;
// byte com = in.get() ; // 压缩位
//
// int packetHeader = s - HEADER_SIZE;
// decoderState.packetlength = packetHeader;
//// minaLog.debug("IoSession with {} packet Header size {}", session,
// packetHeader);
// if (packetHeader < 0 || packetHeader > Short.MAX_VALUE) { throw new
// IllegalStateException(
// "data length error."); }
// } else if (in.remaining() < HEADER_SIZE && decoderState.packetlength == -1)
// {
//// minaLog.debug("IoSession with {} not enough data", session);
// return false;
// }
//
// if (in.remaining() >= decoderState.packetlength)
// {
// ClientPoker client = (ClientPoker) session.getAttribute("client") ;
// if (client==null) {
// client = new ClientPoker() ;
// client.setIoSession(session);
// session.setAttribute("client") ;
// }
// in.get() ; // 消息类型
// int type = in.getInt() ;
// int callBack = in.getInt() ;
// final int dataPending = decoderState.packetlength;
// decoderState.packetlength = -1;
//
// System.err.println("客户端收到返回消息,包类型: 0x"+Integer.toHexString(type)+" callBack:
// "+callBack);
//
// Connection connection = (Connection)session.getAttribute("con") ;
// if (connection==null)
// {
// connection = new Connection(cid.incrementAndGet(), null,
// session.getRemoteAddress().hashCode()) ;
// session.setAttribute("con", connection) ;
// }
//
// byte[] datas = new byte[in.remaining()] ;
// in.get(datas) ;
//
// Class<?> messageClass =
// ConnectionManager.getInstance().getMsgInitializer().getMessageClass(type);
// Object newInstance = messageClass.newInstance();
// Method method = messageClass.getMethod("parseFrom", byte[].class) ;
// method.invoke(newInstance, datas) ;
//
// BaseMessageHandler<?> messageHandler =
// ConnectionManager.getInstance().getMsgInitializer().getMessageHandler(type);
//
//// ProtobufProtocol pbProtocol = new ProtobufProtocol(type,newInstance) ;
//
//// out.write(pbProtocol) ;
//
// return true;
// } else
// {
//// minaLog.debug("IoSession with {} not enough data to decode (need {})",
// session, decoderState.packetlength);
// return false;
// }
// }
// }
