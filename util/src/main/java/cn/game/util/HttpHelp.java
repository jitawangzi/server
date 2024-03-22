package cn.game.util;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class HttpHelp {
	private static Logger log = LoggerFactory.getLogger(HttpHelp.class);
	private static Vertx vertx = Vertx.vertx();

	public static void postAsync(int port, String host, String url, String body, Consumer<Object> success, Consumer<Object> fail) {

		WebClientOptions options = new WebClientOptions().setUserAgent("My-App/1.2.3");
		options.setKeepAlive(false);
		WebClient client = WebClient.create(vertx, options);

		client.post(port, host, "url").sendBuffer(Buffer.buffer(body), ar -> {
			if (ar.succeeded()) {
				io.vertx.ext.web.client.HttpResponse<Buffer> result = ar.result();
				if (success != null) {
					success.accept(result.body());
				}
			} else {
				if (fail != null) {
					fail.accept(ar.cause());
				}
				log.error("", ar.cause());
			}
		});

	}

	public static void sendForm() {

		WebClientOptions options = new WebClientOptions().setUserAgent("My-App/1.2.3");
		options.setKeepAlive(false);
		WebClient webClient = WebClient.create(vertx, options);
		MultiMap multiMap = MultiMap.caseInsensitiveMultiMap();
		multiMap.add("name", "yitian");
		multiMap.add("age", "25");
		webClient.post("httpbin.org", "/post").sendForm(multiMap, ar -> {
			if (ar.succeeded()) {
				io.vertx.ext.web.client.HttpResponse<Buffer> response = ar.result();
				System.out.println(response.body());
			}
		});
	}

	public enum HttpMethodType {
		GET, POST
	}

	public static void main(String args[]) {
		postAsync(13333, "127.0.0.1", "/index.html", "a=b", System.out::print, null);
	}
}
