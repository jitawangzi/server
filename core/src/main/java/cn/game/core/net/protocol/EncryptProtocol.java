package cn.game.core.net.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.mina.core.buffer.IoBuffer;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class EncryptProtocol extends BaseProtocol {

	public static final int MIN_MESSAGE_LENGTH = 1;// 最小消息长度的字节数

	public static final int MAX_MESSAGE_LENGTH = 65535;// 最大消息长度的字节数

	static final int HEADER_SIZE = 4;// 消息头长度

	static final int HEADER_LEN_BYTES = 2;// 消息长标志位长度

	static final int COMPRESS_MASK_BYTES = 1;// 压缩标志位长度

	static final int ENCRYPT_KEY_BYTES = 1;// 秘钥位长度

	/**
	 * 压缩临界点
	 */
	static public final int SIZE_OF_PKG_COMPRESS = 1024;

	protected IoBuffer buf;

	protected int position = 0;

	private int encrypt;

	private byte compress;
	// private short messageType;
	protected byte retCode = 0; // 返回码
	protected String debugMsg = null; // 错误调试文字
	protected int errColor = 0; // 错误提示文字颜色
	protected String errMsg = ""; // 错误提示文字

	public EncryptProtocol() {
		buf = null;

		messageLength = 0;
		// messageType = MessageType.MSG_UNKNOWN;

	}

	public void setBuffer(IoBuffer buf) {
		this.buf = buf;
	}

	@JsonIgnore
	public IoBuffer getBuffer() {
		return buf;
	}

	@JsonIgnore
	public int getEncrypt() {
		return encrypt;
	}

	public void setEncrypt(int encrypt) {
		this.encrypt = encrypt;
	}

	@JsonIgnore
	public byte getCompress() {
		return compress;
	}

	public void setCompress(byte compress) {
		this.compress = compress;
	}
	protected void writeHeader() {
		// 此时长度才是有效值
		messageLength = buf.position() - (position + HEADER_LEN_BYTES);

		buf.putShort(position, (short) messageLength);
	}

	/**
	 * 压缩
	 * @return
	 * @throws IOException
	 */
	private boolean compress() throws IOException {

//		if (compress == ICompressorProtocol.FORCE_NOT_COMPRESS) {
//			return false;
//		}

//		if (compress == ICompressorProtocol.FORCE_NORMAL) {
//
//			if (messageLength < SIZE_OF_PKG_COMPRESS) {
//				return false;
//			}
//		}

		buf.put(position + HEADER_LEN_BYTES, (byte) 1);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		GZIPOutputStream zos = new GZIPOutputStream(bos);
		byte[] data = new byte[messageLength - COMPRESS_MASK_BYTES];
		buf.position(position + HEADER_LEN_BYTES + COMPRESS_MASK_BYTES);
		buf.get(data, 0, data.length);
		zos.write(data);
		zos.close();

		data = bos.toByteArray();
		buf.position(position + HEADER_LEN_BYTES + COMPRESS_MASK_BYTES);
		buf.put(data);
		bos.close();

		return true;
	}

	/**
	 * 加密
	 * @return
	 */
	private void encode() {

		byte[] data = buf.array();

		int resume = 0;
		byte s_crc1 = (byte) ((~(encrypt % 0x100)) & 0xff);
		for (int i = position + HEADER_SIZE, len = buf.position(); i < len; i++) {
			resume += data[i];
			data[i] = encryptByte(data[i], s_crc1);
		}
		byte s_crc2 = (byte) ((resume % 0x100) & 0xff);

		buf.put(position + HEADER_LEN_BYTES + COMPRESS_MASK_BYTES, encryptByte(s_crc2, s_crc1));
	}

	private byte encryptByte(byte b, byte key) {
		int i = b & 0xff;
		i = (i + key) % 0x100;
		b = (byte) ((~(i & 0xff)) & 0xff);
		b = (byte) ((b ^ (key << 1)) & 0xff);
		return b;
	}

	/**
	 * 效率低，尽量不要调用
	 */
	@Override
	public String toString() {
		return StringBuilder.reflectionToString(this, PROTOCOL_STYLE);
	}

	public static final ToStringStyle PROTOCOL_STYLE = new ProtocolToStringStyle();

	private static final class StringBuilder extends ReflectionToStringBuilder {

		public StringBuilder(EncryptProtocol protocol) {
			super(protocol);
		}

		@Override
		protected boolean accept(Field field) {
			return (super.accept(field)) && (field.getName() != "buf") && (field.getName() != "position")
					&& (field.getName() != "compress");
		}
	}

	private static final class ProtocolToStringStyle extends ToStringStyle {

		private static final long serialVersionUID = 1L;

		/**
		 * <p>Constructor.</p>
		 *
		 * <p>Use the static constant rather than instantiating.</p>
		 */
		ProtocolToStringStyle() {
			super();
			this.setUseClassName(false);
			this.setUseIdentityHashCode(false);
			this.setUseFieldNames(false);
			this.setArrayStart("[");
			this.setArrayEnd("]");
			this.setFieldSeparator("|");
			this.setContentStart("{");
			this.setContentEnd("}");
		}

	}

}
