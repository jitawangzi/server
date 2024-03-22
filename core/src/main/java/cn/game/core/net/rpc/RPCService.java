package cn.game.core.net.rpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.net.transport.Command;

public interface RPCService<T> {

	public static final Logger log = LoggerFactory.getLogger(RPCService.class);

	public void init();

	public void start();

	public T getWrappedService();

	default Object invoke(Command command)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {

		if (command.getArgs() != null && command.getArgs().length > 0) {
			Method m = getWrappedService().getClass().getMethod(command.getMethodName(), command.getClazz());
			return m.invoke(getWrappedService(), command.getArgs());
		} else {
			Method m = getWrappedService().getClass().getMethod(command.getMethodName());
			return m.invoke(getWrappedService());
		}
	}

	public boolean shutdown();

}
