package cn.game.games.core.netty;

import java.util.List;

import cn.game.core.net.protocol.object.ProtobufProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

public class WebSocketEncoder extends MessageToMessageEncoder<ProtobufProtocol> {

	@Override
	protected void encode(ChannelHandlerContext ctx, ProtobufProtocol msg, List<Object> out) throws Exception {
		byte[] datas = msg.getData().toByteArray();

		CompositeByteBuf compositeBuffer = Unpooled.compositeBuffer(2);
		ByteBuf headerBuf = Unpooled.buffer(12);
		headerBuf.writeInt(datas.length + 12);
		headerBuf.writeInt(msg.getMsgID());
		headerBuf.writeInt(msg.getErrorCode());
		ByteBuf bodyBuf = Unpooled.wrappedBuffer(datas);
		compositeBuffer.addComponents(headerBuf, bodyBuf);
		compositeBuffer.writerIndex(headerBuf.readableBytes() + bodyBuf.readableBytes());

		out.add(new BinaryWebSocketFrame(compositeBuffer));
	}

}
