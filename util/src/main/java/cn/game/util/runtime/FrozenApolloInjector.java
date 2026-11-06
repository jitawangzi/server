package cn.game.util.runtime;

import com.ctrip.framework.apollo.internals.ConfigManager;
import com.ctrip.framework.apollo.spi.ApolloInjectorCustomizer;

/** Apollo 首次创建 ConfigService 时即接管严格模式；普通启动保持默认实现。 */
public final class FrozenApolloInjector implements ApolloInjectorCustomizer {
    @Override public int getOrder() { return Integer.MIN_VALUE; }
    @Override public <T> T getInstance(Class<T> type) {
        return FrozenApollo.enabled() && type == ConfigManager.class ? type.cast(FrozenApollo.INSTANCE) : null;
    }
    @Override public <T> T getInstance(Class<T> type, String name) { return null; }
}
