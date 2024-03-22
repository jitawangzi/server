package cn.game.util.db;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.springframework.beans.factory.annotation.Autowired;

import com.sun.net.httpserver.HttpServer;

import io.micrometer.prometheus.PrometheusMeterRegistry;

public class HikariReporter {

	@Autowired
	private PrometheusMeterRegistry prometheusRegistry ;

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

}
