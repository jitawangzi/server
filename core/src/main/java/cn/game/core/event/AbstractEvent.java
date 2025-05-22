package cn.game.core.event;


public abstract class AbstractEvent<T> {
	/** 事件类型 */
	protected T type;
	protected Object[] params; // 事件参数
	protected final static transient Object[] NO_PARAM = new Object[0];

	public AbstractEvent(T type, Object... params) {
		
		this.type = type;
		this.params = params == null ? NO_PARAM : params;
	}

	public AbstractEvent(T type) {
		this(type,NO_PARAM)  ;
	}

	/**
	 * 只用来传递参数
	 * 
	 * @param params
	 */
	public AbstractEvent(Object... params) {
		this(null, params);
	}

	public T getType() {
		return type;
	}

	/**
	 * 获取第i各参数
	 * 
	 * @param i
	 * @return
	 */
	@SuppressWarnings({ "hiding", "unchecked" })
	public final <T> T getParameter(int i) {

		return (T) params[i];
	}

	/**
	 * 获取String型参数
	 * 
	 * @param i
	 * @return
	 */
	public final String getStringParameter(int i) {

		return (String) params[i];
	}

	/**
	 * 获取bool型参数
	 * 
	 * @param i
	 * @return
	 */
	public final boolean getBoolParameter(int i) {

		return ((Boolean) params[i]).booleanValue();
	}

	/**
	 * 获取byte型参数
	 * 
	 * @param i
	 * @return
	 */
	public final byte getByteParameter(int i) {

		return ((Byte) params[i]).byteValue();
	}

	/**
	 * 获取short型参数
	 * 
	 * @param i
	 * @return
	 */
	public final short getShortParameter(int i) {

		return ((Short) params[i]).shortValue();
	}

	/**
	 * 获取int型参数
	 * 
	 * @param i
	 * @return
	 */
	public final int getIntParameter(int i) {
		return ((Integer) params[i]).intValue();
	}

	public final int get(int i) {
		if (params[i] instanceof Integer) {
			return ((Integer) params[i]).intValue();
		}
		return Integer.parseInt(params[i].toString());
	}

	public final long getLong(int i) {
		if (params[i] instanceof Long) {
			return ((Long) params[i]).longValue();
		}
		return Long.parseLong(params[i].toString());
	}

	/**
	 * 获取long型参数
	 * 
	 * @param i
	 * @return
	 */
	public final long getLongParameter(int i) {

		return ((Long) params[i]).longValue();
	}

	public final float getFloatParameter(int i) {

		return ((Float) params[i]).floatValue();
	}

	/**
	 * 是否有参数
	 * 
	 * @param i
	 *            参数顺序
	 * @return
	 */
	public final boolean hasParameter(int i) {
		return i >= params.length;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append(type)
				.append("|");
		for (Object para : params) {
			sb.append(para).append("|");
		}
		return sb.toString();
	}

	// ///////////////////////getter & setter//////////////////////////////

	/**
	 * @return the params
	 */
	public Object[] getParams() {

		return params;
	}

}
