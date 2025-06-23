package cn.game.util.db;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

import io.micrometer.core.instrument.Counter;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;

@Deprecated
public class HikariReporter {

//	@Autowired
	private PrometheusMeterRegistry prometheusRegistry;

	// 初始化vertx时已经启动内置httpserver了
	@Deprecated
	public void init() {
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress(9666), 0);
			server.createContext("/prometheus", httpExchange -> {
				String response = prometheusRegistry.scrape();
				httpExchange.sendResponseHeaders(200, response.getBytes().length);
				try (OutputStream os = httpExchange.getResponseBody()) {
					os.write(response.getBytes());
				}
			});
			new Thread(server::start).start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// 添加自定义指标
	public void registerCustomMetrics() {
		Counter.builder("hikari.custom.metric").description("Custom Hikari metric").register(prometheusRegistry);
	}
}