package cn.game.core.async;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface AsyncProcessor<T> {
	CompletableFuture<?> process(T item);
}