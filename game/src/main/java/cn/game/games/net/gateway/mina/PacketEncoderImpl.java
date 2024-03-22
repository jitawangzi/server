package cn.game.games.net.gateway.mina;

import java.nio.ByteOrder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.zeromq.ZMsg;

import cn.game.util.ByteHelp;


public class PacketEncoderImpl implements ProtocolEncoder

{
	private static final int				PACKET_LENGTH		= 2;

	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception
	{
		
		ZMsg zMsg = (ZMsg)message ; 
		
//		byte[] checkId = zMsg.pop().getData(); 
		byte[] id = zMsg.peekFirst().getData(); 
		byte[] data = zMsg.peekLast().getData() ; 
		
		short len = (short) (id.length+8+data.length+PACKET_LENGTH) ; 
		IoBuffer buf = IoBuffer.allocate(len).order(ByteOrder.LITTLE_ENDIAN);
		buf.putShort(len) ; 
		buf.putLong(0) ;  // checkid
		 
		buf.put(id) ; 
		buf.put(data) ; 
		buf.flip() ; 
		
		out.write(buf);
	}

	@Override
	public void dispose(IoSession session) throws Exception
	{
	}
}
