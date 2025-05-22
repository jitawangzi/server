package cn.game.games.core.event;

import cn.game.core.event.AbstractEvent;
import cn.game.games.cache.entity.Player;

/**
 * 玩家事件
 * 
 * @author syq
 * 
 */
public class PlayerEvent extends AbstractEvent<EventTypeEnum> {

	// private final Source sourceTag; //事件源标识
	private final transient Player source, target; // 事件的发起者和目标（target may be null）
	private long sourceId;
	private transient volatile boolean isSendToCross = false;

	/**
	 * 
	 * @param type
	 * @param source
	 */
	public PlayerEvent(EventTypeEnum type, Player source) {

		this(type, source, null, NO_PARAM);
	}

	/**
	 * 
	 * @param type
	 * @param source
	 * @param target
	 */
	public PlayerEvent(EventTypeEnum type, Player source, Player target) {

		this(type, source, target, NO_PARAM);
	}

	/**
	 * 
	 * @param type
	 * @param source
	 * @param params
	 */
	public PlayerEvent(EventTypeEnum type, Player source, Object... params) {

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
	public PlayerEvent(EventTypeEnum type, Player source, Player target, Object... params) {

		this.source = source;
		this.target = target;
		super.type = type;
		super.params = params;
	}
	public PlayerEvent(EventTypeEnum type,Object... params) {
		
		this(type,null,null,params)  ;
	}
	public PlayerEvent(EventTypeEnum type) {
		
		this(type,null,null,NO_PARAM)  ;
	}
	/**
	 * 只用来传递参数
	 * 
	 * @param params
	 */
	public PlayerEvent(Object... params) {

		this(null, null, null, params);
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
