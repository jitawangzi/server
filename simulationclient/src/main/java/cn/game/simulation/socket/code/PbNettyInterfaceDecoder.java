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
// import client.ProtobufProtocol;
// import cn.game.util.ByteHelp;
//
//
// public class PbNettyInterfaceDecoder extends CumulativeProtocolDecoder
// {
//
// private static final String DECODER_STATE_KEY =
// PbNettyInterfaceDecoder.class.getName() + ".STATE";
// private static final int HEADER_SIZE = 4;
// private static AtomicInteger cid = new AtomicInteger(1) ;
//
// public PbNettyInterfaceDecoder()
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
// if (packetHeader < 0 || packetHeader > Short.MAX_VALUE)
// {
// // 可能有个6长度的netty心跳包，跳过吧 。
// in.get(new byte[in.remaining()]) ;
// return true;
//// throw new IllegalStateException("data length error.");
// }
// } else if (in.remaining() < HEADER_SIZE && decoderState.packetlength == -1)
// {
//// minaLog.debug("IoSession with {} not enough data", session);
// return false;
// }
//
// if (in.remaining() >= decoderState.packetlength)
// {
// int t = in.get() ; // 消息类型
// int type = in.getInt() ;
// int callBack = in.getInt() ;
// final int dataPending = decoderState.packetlength;
// decoderState.packetlength = -1;
//
// byte[] datas = new byte[in.remaining()] ;
// in.get(datas) ;
//
// Class<?> messageClass =
// ConnectionManager.getInstance().getMsgInitializer().getMessageClass(type);
// Method method = messageClass.getMethod("parseFrom", byte[].class) ;
// Object msg = null ;
// if (method!=null)
// {
// msg = method.invoke(null,datas) ;
// }
// ProtobufProtocol pbProtocol = new ProtobufProtocol(type,msg) ;
// pbProtocol.callback = callBack ;
//
// out.write(pbProtocol) ;
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
