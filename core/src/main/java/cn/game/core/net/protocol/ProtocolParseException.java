package cn.game.core.net.protocol;

public class ProtocolParseException extends Exception {
	
	private static final long serialVersionUID = 1L;
	/** 用于指示消息来源的链路是否已经被破坏,如果已经被破坏,需要强制关闭对应的链路;否则可以不关闭 */
	private volatile boolean broken = true;

	public ProtocolParseException() {
		super();
	}

	public ProtocolParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProtocolParseException(String message) {
		super(message);
	}

	public ProtocolParseException(Throwable cause) {
		super(cause);
	}

	/**
	 * 用于指示消息来源的链路是否已经被破坏,如果已经被破坏,需要强制关闭对应的链路不
	 * 
	 * @return the broken true,链路已经被破坏,必须关闭链路;false,链路还可以继续使用,可以不关闭链路
	 */
	public final boolean isBroken() {
		return broken;
	}

	/**
	 * @param broken
	 *            the broken to set
	 */
	public final void setBroken(boolean broken) {
		this.broken = broken;
	}
}
