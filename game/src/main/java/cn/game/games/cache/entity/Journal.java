package cn.game.games.cache.entity;

import java.io.Serializable;


public class Journal implements Serializable {
	
	/**  */
	private static final long	serialVersionUID	= -2780739647301865375L;
	private String day ; 
	private String hour ; 
	private String msg ;
	
	
	public String getDay() {
	
		return day;
	}
	
	public void setDay(String day) {
	
		this.day = day;
	}

	
	public String getHour() {
	
		return hour;
	}

	
	public void setHour(String hour) {
	
		this.hour = hour;
	}

	public String getMsg() {
	
		return msg;
	}
	
	public void setMsg(String msg) {
	
		this.msg = msg;
	} 

}
