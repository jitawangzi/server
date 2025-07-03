package cn.game.core.execute.error;

import java.util.function.Function;

/**
 * 任务执行失败时的处理器接口
 * <p>
 * 通过函数式接口定义，可以方便地使用lambda表达式创建实例
 */
@FunctionalInterface
public interface ErrorHandler extends Function<RetryContext, ErrorPolicy> {

    /**
     * 根据失败上下文决定下一步的错误处理策略
     *
     * @param context 失败上下文，包含任务、异常、重试次数等信息
     * @return 错误处理策略
     */
    @Override
    ErrorPolicy apply(RetryContext context);

    /**
     * 一个默认的错误处理器，总是放弃任务
     */
    ErrorHandler DISCARD_HANDLER = context -> ErrorPolicy.discard();
}

