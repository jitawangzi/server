package cn.game.simulation.socket.code;
// package socket.base.code;
//
// import java.nio.ByteOrder;
//
// import org.apache.mina.core.buffer.IoBuffer;
// import org.apache.mina.core.session.IoSession;
// import org.apache.mina.filter.codec.ProtocolEncoder;
// import org.apache.mina.filter.codec.ProtocolEncoderOutput;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
//
// import com.google.protobuf.Message;
// import com.kodgames.corgi.core.session.ConnectionManager;
//
//
// public class PbNettyEncoder implements ProtocolEncoder
// {
// private static final int LENGTH = 2 ;
// private static Logger log = LoggerFactory.getLogger(PbNettyEncoder.class);
// private static Logger netdownLog = LoggerFactory.getLogger("gamenetdownLog");
//
// @Override
// public void encode(IoSession session, Object message, ProtocolEncoderOutput
// out) throws Exception
// {
//
// Message msg = (Message) message ;
// byte[] byteArray = msg.toByteArray() ;
// System.err.println("发送包"+ message.getClass().getSimpleName());
//// int msgId =
// Integer.parseInt(message.getClass().getSimpleName().split("_")[1],16) ;
// // 1+4+4 messageType+protocolID+callback
// int len = 4+byteArray.length+1+4+4+1+5;
// IoBuffer buf = IoBuffer.allocate(len).order(ByteOrder.BIG_ENDIAN);
//
// if (byteArray.length>Short.MAX_VALUE) {
// System.err.println("pack too big :"+byteArray.length);
// return ;
// }
// // 直连game需要多一个压缩位，少一个站位
// buf.putInt(len-4) ;
// buf.put((byte)0) ; // 压缩位
// buf.put((byte)0x01) ; // 消息类型
// buf.put((byte)0x4a) ; // secretKey
//// buf.put((byte)0); // 站位
// buf.putInt(666) ; // dstPeerID
//// buf.putInt(333);
//// buf.putInt(444);
//// buf.putInt(555);
//
// int msgId =
// ConnectionManager.getInstance().getMsgInitializer().getProtocolID(message.getClass());
// buf.putInt(msgId) ;
// buf.putInt(55555) ; //callBack
// buf.put(byteArray) ;
// buf.flip() ;
// out.write(buf);
//
// }
//
// @Override
// public void dispose(IoSession session) throws Exception
// {
//
// }
// }
