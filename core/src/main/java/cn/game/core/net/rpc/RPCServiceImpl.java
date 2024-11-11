package cn.game.core.net.rpc;

import java.lang.reflect.Method;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.net.transport.Command;
import cn.game.util.ServerType;
import cn.game.util.reflect.ClassHelper;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

public class RPCServiceImpl<T> implements RPCService<T> {
	protected static final Logger log = LoggerFactory.getLogger(RPCServiceImpl.class);

    protected T wrappedService;
	public String serverId;
	public ServerType serverType;
	public Vertx vertx;

	public RPCServiceImpl(T wrappedService, String serverId, ServerType serverType) {
        this.wrappedService = wrappedService;
        this.serverId = serverId;
		this.serverType = serverType;
    }

    @Override
    public T getWrappedService() {
        return wrappedService;
    }

    @Override
    public void init() {
        // 默认实现，可以被子类覆盖
    }

//    @Override
//    public void start() {
//        // 默认实现，可以被子类覆盖
//    }

    public Object invokeWithCache(Command command) throws Throwable {
		Method method = ClassHelper.findMethod(wrappedService.getClass(), command.getMethodName(), command.getParameterType());
        return method.invoke(wrappedService, command.getArgs());
    }

    public void handleResult(Object result, Promise<Object> promise) {
        if (result instanceof io.vertx.core.Future) {
            ((io.vertx.core.Future<Object>) result).onComplete(promise);
        } else if (result instanceof CompletionStage) {
            ((CompletionStage<Object>) result).whenComplete((res, ex) -> {
                if (ex != null) {
                    promise.fail(ex);
                } else {
                    promise.complete(res);
                }
            });
        } else if (result instanceof Future) {
            handleJavaFuture((Future<Object>) result, promise);
        } else if (result instanceof Throwable) {
            promise.fail((Throwable) result);
        } else {
            promise.complete(result);
        }
    }

    public void handleJavaFuture(Future<Object> future, Promise<Object> promise) {
        if (future.isDone()) {
            try {
                Object result = future.get();
                promise.complete(result);
            } catch (Exception e) {
                promise.fail(e);
            }
            return;
        }
        long timerId = vertx.setPeriodic(10, id -> {
            if (future.isDone()) {
                vertx.cancelTimer(id);
                try {
                    Object result = future.get();
                    promise.complete(result);
                } catch (Exception e) {
                    promise.fail(e);
                }
            }
        });
        vertx.setTimer(20000, id -> {
            vertx.cancelTimer(timerId);
            if (!promise.future().isComplete()) {
                promise.fail(new TimeoutException("Future did not complete within 20 seconds"));
            }
        });
    }

    @Override
    public boolean shutdown() {
        log.info("{} starting shutdown...", this.getClass().getSimpleName());
        // 实现优雅关闭逻辑
        return true;
    }

    // 自定义RPC异常
    public static class RPCException extends RuntimeException {
        public RPCException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}