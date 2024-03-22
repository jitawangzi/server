package cn.game.games.core.event;

public class GameParam {

	private final Object[]				params;						// 事件参数
	private final static Object[]		NO_PARAM	= new Object[0];

	/**
	 * 
	 * @param params
	 */
	public GameParam(Object... params) {
		this.params = params;
	}
	public GameParam() {
		this.params = NO_PARAM;
	}

	/**
	 * 获取第i各参数
	 * 
	 * @param i
	 * @return
	 */
	public final Object getParameter(int i) {

		return params[i];
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

	/**
	 * @return the params
	 */
	public Object[] getParams() {

		return params;
	}

}
