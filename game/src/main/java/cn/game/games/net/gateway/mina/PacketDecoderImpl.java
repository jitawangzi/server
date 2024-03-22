package cn.game.games.net.gateway.mina;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMsg;

import cn.game.util.ByteHelp;
import cn.game.games.net.client.GateClient;

public class PacketDecoderImpl extends CumulativeProtocolDecoder
{
	private static Logger		log				= LoggerFactory.getLogger(PacketDecoderImpl.class);
	private final static Logger				minaLog				= LoggerFactory.getLogger("minaLog");
	private static Logger					netupLog			= LoggerFactory.getLogger("gamenetupLog");
	private static final String				DECODER_STATE_KEY	= PacketDecoderImpl.class.getName() + ".STATE";
	private static final int				HEADER_SIZE			= 2;
	public static final int MAX_BODY_LEN = 1024;
	
	

	private class DecoderState
	{
		public int	packetlength	= -1;
	}
	
	public PacketDecoderImpl()
	{
	}


	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception
	{
		
		// 数据包：  4个长度的length(header) + pack body .

		DecoderState decoderState = (DecoderState) session.getAttribute(DECODER_STATE_KEY);
		if (decoderState == null)
		{
			decoderState = new DecoderState();
			session.setAttribute(DECODER_STATE_KEY, decoderState);
		}

		// 没有开始解析这个包，如果剩余数据大于包头，则开始读取，否则返回false等待后续数据
		if (in.remaining() >= HEADER_SIZE && decoderState.packetlength == -1)
		{
//			in.array()
			// in.getInt()为这个包的数据总长度，减去header 为body的数据长度
//			int i = ByteBuffer.wrap(fourBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
			
			int packetHeader = in.getShort() - HEADER_SIZE;
			decoderState.packetlength = packetHeader;
			minaLog.debug("IoSession with {} packet Header size {}", session, packetHeader);
			if (packetHeader < 0 || packetHeader > MAX_BODY_LEN) { throw new IllegalStateException(	"data length error."); }
		} else if (in.remaining() < HEADER_SIZE && decoderState.packetlength == -1)
		{
			minaLog.debug("IoSession with {} not enough data", session);
			return false;
		}

		if (in.remaining() >= decoderState.packetlength)
		{
//			final int dataPending = decoderState.packetlength;
			decoderState.packetlength = -1;
			// 准备读取数据
//			int pos = in.position();
			GateClient client = (GateClient) session.getAttribute(GateClient.CLIENT_KEY);
			
//			session.setAttribute("playerId", playerId) ; 
			// 用点简单的方法吧，读取包类型
//			int type  = in.getInt() ; 
//			int type = (in.get() & 0x000000ff) + ((in.get() & 0x000000ff) << 8);
//			int type = (in.get()) + ((in.get()) << 8);
			
			// 解密
//			GameCrypt.gameCrypt.decrypt(in, type, dataPending - 10, pos + 10);
//			if (client == null)
//			{
//				// 创建一个新一客户端
//				client = new GateClient(GameCrypt.gameCrypt, session);
//				session.setAttribute(GateClient.CLIENT_KEY, client);
//				InetSocketAddress isa = (InetSocketAddress) session.getRemoteAddress();
//				client.setIp(isa.getAddress().getHostAddress());
//				
//				GateClientManager.getInstance().addGateClient(client); 
//				
//				log.debug("创建新session，id ：{}",client.getSessionId());
//			}
			
//			long playerId = client.getPlayerId() ; 

//			if (log.isDebugEnabled())
//			{
//				netupLog.debug("opType[receive]playId[{}]type[0x{}]size[{}]", new Object[]
//						{playerId,Integer.toHexString(0), dataPending });
//			}
			
		} else
		{
			// body数据没读完
			minaLog.debug("IoSession with {} not enough data to decode (need {})", session, decoderState.packetlength);
			return false;
		}
		
		if (in.remaining() <= 0)
		{
			return false;
		}
		ZMsg msg = new ZMsg() ; 
		long checkId = in.getLong() ; 
		int msgId = in.getInt() ; 
//		System.err.println("网关服收到消息："+Integer.toHexString(msgId));
		msg.add(ByteHelp.toByteArrayB(msgId)) ; 
//		msg.add(ByteHelp.toByteArrayB(checkId)) ; 
		byte[] data = new byte[in.remaining()];
		in.get(data);
		msg.add(data) ; 
		
		out.write(msg);
		return true;
//		
		
		
	}
}