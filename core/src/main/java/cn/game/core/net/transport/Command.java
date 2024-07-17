package cn.game.core.net.transport;

import java.io.Serializable;
import java.util.Arrays;

import com.alibaba.fastjson.JSON;

/**
 * 表示远程调用时的命令
 * 2016年10月26日 下午2:30:21
 * @author SYQ
 */
public class Command implements Serializable {

	/**  */
	private static final long serialVersionUID = 8797909840036157425L;
	private String methodName;
	private Object[] args;
	private Class<?>[] clazz;

	public Command() {
	}

	public Command(String methodName, Class<?>[] clazz, Object[] args) {
		this.methodName = methodName;
		this.clazz = clazz;
		this.args = args;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	public Class<?>[] getClazz() {
		return clazz;
	}

	public void setClazz(Class<?>[] clazz) {
		this.clazz = clazz;
	}
	@Override
	public String toString() {
		return "Command [methodName=" + methodName + ", args=" + JSON.toJSONString(args) + ", clazz=" + Arrays.toString(clazz) + "]";
	}

}
