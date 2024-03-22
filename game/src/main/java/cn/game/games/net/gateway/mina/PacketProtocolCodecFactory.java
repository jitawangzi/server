package cn.game.games.net.gateway.mina;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class PacketProtocolCodecFactory implements ProtocolCodecFactory
{

	private PacketEncoderImpl	encoder;
	private PacketDecoderImpl	decoder;

	public void setEncoder(PacketEncoderImpl encoder)
	{
		this.encoder = encoder;
	}

	public void setDecoder(PacketDecoderImpl decoder)
	{
		this.decoder = decoder;
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession arg0) throws Exception
	{

		return decoder;
	}

	@Override
	public ProtocolEncoder getEncoder(IoSession arg0) throws Exception
	{

		return encoder;
	}

}
