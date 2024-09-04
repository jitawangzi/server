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
// import client.ClientPoker;
// import client.ProtobufProtocol;
//
//
// public class PbNettyInterfaceEncoder implements ProtocolEncoder
// {
// private static final int LENGTH = 2 ;
// private static Logger log =
// LoggerFactory.getLogger(PbNettyInterfaceEncoder.class);
// private static Logger netdownLog = LoggerFactory.getLogger("gamenetdownLog");
//
// @Override
// public void encode(IoSession session, Object message, ProtocolEncoderOutput
// out) throws Exception
// {
//
//// ProtobufProtocol p = (ProtobufProtocol)message ;
//
//// Message msg = (Message) p.datas ;
// Message msg = (Message) message ;
//// byte msgType = p.type ;
// byte[] byteArray = msg.toByteArray() ;
// // 1+4+4 messageType+protocolID+callback
// int len = 4+byteArray.length+1+4+4+1+5;
// IoBuffer buf = IoBuffer.allocate(len).order(ByteOrder.BIG_ENDIAN);
//
// if (byteArray.length>Short.MAX_VALUE) {
// System.err.println("pack too big :"+byteArray.length);
// return ;
// }
// ClientPoker client = (ClientPoker)session.getAttribute("client") ;
// int msgId =
// ConnectionManager.getInstance().getMsgInitializer().getProtocolID(message.getClass());
// // 直连game需要多一个压缩位，少一个占位
//// buf.putInt(len-4) ;
//// buf.put((byte)0) ; // 压缩位
//// if
// (message.getClass().getSimpleName().equalsIgnoreCase("CGOfflineEnrollAreaListREQ"))
// if (client.messageType==(byte)0x05)
// {
// buf.putInt(2) ;
// client.messageType = (byte)0x01 ;
// buf.put((byte)0x05) ; // 消息类型 0x05心跳
// buf.put((byte)0x4a) ; // secretKey
//
// }else
// {
// buf.putInt(len-4) ;
//
// buf.put((byte)0x01) ; // 消息类型 0x05心跳
// buf.put((byte)0x4a) ; // secretKey
// buf.put((byte)0); // 占位
// if
// (message.getClass().getSimpleName().indexOf("Compe")>-1&&message.getClass().getSimpleName().equalsIgnoreCase("CGCompetitionHistoryREQ")==false)
// {
// buf.putInt(client.competitionServerId) ; // dstPeerID
//
// }else if (message.getClass().getSimpleName().indexOf("CBEnterRoom")>-1)
// {
// buf.putInt(client.battleServerId) ; // dstPeerID
//
// }else if (message.getClass().getSimpleName().indexOf("CMOrderREQ")>-1)
// {
// buf.putInt(client.manageServerid) ; // dstPeerID
//
// }else {
//
// buf.putInt(client.gameServerId) ; // dstPeerID
// }
//
// buf.putInt(msgId) ;
// buf.putInt(client.callback) ; //callBack
// buf.put(byteArray) ;
// }
//
// buf.flip() ;
// out.write(buf);
//
//// log.info("客户端发送包: "+ message.getClass().getSimpleName()+"\t"+message);
//
// }
//
// @Override
// public void dispose(IoSession session) throws Exception
// {
//
// }
// }
