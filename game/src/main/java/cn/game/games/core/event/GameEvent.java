package cn.game.games.core.event;

import cn.game.games.cache.entity.Player;

/**
 * 事件
 * 
 * @author syq
 * 
 */
public class GameEvent {

	// ///////////////////////////////////////////////////////////////////
	private final EventTypeEnum				type;							// 事件类别
	// private final Source sourceTag; //事件源标识
	private final transient Player source, target; // 事件的发起者和目标（target may be null）
	private final Object[]				params;						// 事件参数
	private final static transient Object[] NO_PARAM = new Object[0];
	private long sourceId;
	private transient volatile boolean isSendToCross = false;

	/**
	 * 
	 * @param type
	 * @param source
	 */
	public GameEvent(EventTypeEnum type, Player source) {

		this(type, source, null, NO_PARAM);
	}
//	/**
//	 * 
//	 * @param type
//	 * @param source
//	 */
//	public GameEvent(EventTypeEnum type, long playerId) {
//		Player player =  PlayerManager.getInstance().getPlayer(playerId) ; 
//
//		this(type, source, null, NO_PARAM);
//	}

	/**
	 * 
	 * @param type
	 * @param source
	 * @param target
	 */
	public GameEvent(EventTypeEnum type, Player source, Player target) {

		this(type, source, target, NO_PARAM);
	}

	/**
	 * 
	 * @param type
	 * @param source
	 * @param params
	 */
	public GameEvent(EventTypeEnum type, Player source, Object... params) {

		this(type, source, null, params);
	}

	/**
	 * 
	 * @param type
	 * @param source
	 * @param target
	 * @param callback
	 * @param params
	 */
	public GameEvent(EventTypeEnum type, Player source, Player target, Object... params) {

		this.source = source;
		this.target = target;
		this.type = type;
		this.params = params;
	}
	public GameEvent(EventTypeEnum type,Object... params) {
		
		this(type,null,null,params)  ;
	}
	public GameEvent(EventTypeEnum type) {
		
		this(type,null,null,NO_PARAM)  ;
	}
	/**
	 * 只用来传递参数
	 * 
	 * @param params
	 */
	public GameEvent(Object... params) {

		this(null, null, null, params);
	}

	/**
	 * 获取第i各参数
	 * 
	 * @param i
	 * @return
	 */
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
		sb.append(type).append("|").append(source == null ? "" : source.getPlayerId()).append("|").append(
				target == null ? "" : target.getPlayerId()).append("|");
		for (Object para : params)
		{
			sb.append(para).append("|");
		}
		return sb.toString();
	}

	// ///////////////////////getter & setter//////////////////////////////
	/**
	 * @return the type
	 */
	public EventTypeEnum getType() {

		return type;
	}

	/**
	 * @return the source
	 */
	public Player getSource() {

		return source;
	}

	/**
	 * @return the target
	 */
	public Player getTarget() {

		return target;
	}

	/**
	 * @return the params
	 */
	public Object[] getParams() {

		return params;
	}

	public long getSourceId() {
		return sourceId;
	}

	public void setSourceId(long sourceId) {
		this.sourceId = sourceId;
	}

	public boolean isSendToCross() {
		return isSendToCross;
	}

	public void setSendToCross(boolean isSendToCross) {
		this.isSendToCross = isSendToCross;
	}

}
