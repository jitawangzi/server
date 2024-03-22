package cn.game.util;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.core.spi.ContextAwareBase;

public class NothingConfigurator extends ContextAwareBase implements Configurator {

	public NothingConfigurator() {
	};
	@Override
	public void configure(LoggerContext loggerContext) {

		System.err.println("do not init ConsoleAppender");

	}

}
