package cn.game.games.net.game.module.award;

import java.io.Serializable;


/**   
 * @Description 
 * @date 2016-9-30 上午11:29:01
 * @author SYQ
 */
public class UnionApply implements Serializable{
	/**  */
	private static final long	serialVersionUID	= -6377690594215294526L;

	/** 申请人id */
	private long playerId ; 
	
	/** 申请时间 */
	private long date ;

	
	public static UnionApply valueOf(long playerId ){
		
		UnionApply u = new UnionApply() ; 
		u.playerId = playerId ; 
		u.date= System.currentTimeMillis() ; 
		return u ; 
	}
	
	public final long getPlayerId() {
	
		return playerId;
	}

	
	public final void setPlayerId(long playerId) {
	
		this.playerId = playerId;
	}
	
	public final long getDate() {
	
		return date;
	}
	
	public final void setDate(long date) {
	
		this.date = date;
	} 
	
}
