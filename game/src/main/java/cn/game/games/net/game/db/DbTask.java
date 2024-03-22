package cn.game.games.net.game.db;

import java.io.Serializable;

public class DbTask implements Serializable {

	/**  */
	private static final long serialVersionUID = 4320562533756337756L;
	private Class<?> mapper;
	private String method;
	private Object arg;

	public DbTask(Class<?> mapper, String method, Object arg) {
		this.mapper = mapper;
		this.method = method;
		this.arg = arg;
	}

	public DbTask() {
	}

	public Class<?> getMapper() {
		return mapper;
	}

	public void setMapper(Class<?> mapper) {
		this.mapper = mapper;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Object getArg() {
		return arg;
	}

	public void setArg(Object arg) {
		this.arg = arg;
	}

}
