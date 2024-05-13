package cn.game.util;

public class LogbackConfig {

	private boolean init;
	private String logbackPath;

	public static void init(boolean init, String logbackPath) throws Exception
		{
			if (init) {
	
				/*		// 取得当前工作目录
						ClassLoader loader = Thread.currentThread().getContextClassLoader();
						InputStream inputStream = loader.getResourceAsStream(logbackPath);
						ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
						LoggerContext loggerContext = (LoggerContext) loggerFactory;
						// loggerContext.reset();
						//定义一个配置器
						JoranConfigurator configurator = new JoranConfigurator();
						//将当前应用的loggercontext关联到configurator对象
						configurator.setContext(loggerContext);
						System.out.println("logback config path : " + (logbackPath));
						//接收从命令行传入的参数，加载配置文件，并设置到配置器
						configurator.doConfigure(inputStream);*/
	
				//StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
			}
		}

		public void init2() throws Exception {
			init(init, logbackPath);
		}

	public void setInit(boolean init) {
		this.init = init;
	}

	public void setLogbackPath(String logbackPath) {
		this.logbackPath = logbackPath;
	}
}