package cn.game.core.async;

import io.vertx.core.Future;

@FunctionalInterface
public interface AsyncProcessor<T> {
	Future<?> process(T item);
}