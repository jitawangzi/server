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
	/** 方法所在类 */
	private String className;
	/** 方法名 */
	private String methodName;
	/** 方法参数类型 */
	private Class<?>[] parameterType;
	/** 方法参数值 */
	private Object[] args;
	/** 用来分线程的 */
	private long objectId;

	public Command() {
	}

	public Command(String className, String methodName, Class<?>[] parameterType, Object[] args, long objectId) {
		this.className = className;
		this.methodName = methodName;
		this.parameterType = parameterType;
		this.args = args;
		this.objectId = objectId;
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

	public Class<?>[] getParameterType() {
		return parameterType;
	}

	public void setParameterType(Class<?>[] parameterType) {
		this.parameterType = parameterType;
	}

	public long getObjectId() {
		return objectId;
	}

	public void setObjectId(long objectId) {
		this.objectId = objectId;
	}


	public String getClassName() {
		return className;
	}

	public String getParameterDesc() {
		if (this.parameterType == null) {
			return "" ; 
		}
		StringBuilder sb = new StringBuilder();
		for (Class clazz : this.parameterType) {
			sb.append(clazz.getName()).append("_");
		}
		return sb.toString();
	}
	@Override
	public String toString() {
		return "Command [methodName=" + methodName + ", args=" + JSON.toJSONString(args) + ", parameterType="
				+ Arrays.toString(parameterType) + "]";
	}

}
