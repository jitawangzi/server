package cn.game.util;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class HttpHelp {
	private static Logger log = LoggerFactory.getLogger(HttpHelp.class);
	private static Vertx vertx = Vertx.vertx();

	public static void postAsync(int port, String host, String url, String body, Consumer<Object> success, Consumer<Object> fail) {

		WebClientOptions options = new WebClientOptions().setUserAgent("My-App/1.2.3");
		options.setKeepAlive(false);
		WebClient client = WebClient.create(vertx, options);

		client.post(port, host, "url").sendBuffer(Buffer.buffer(body)).onComplete(ar -> {
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

	public static void postJSonUrl( String url, Object body,Consumer<String> successFun , Consumer<Exception> failFun) {

		WebClient client = WebClient.create(vertx);
		Future<HttpResponse<Buffer>> reslutFuture = client.postAbs(url)
//				.putHeader("Content-Type", "charset=utf-8")
				.sendJson(body);
		try {
      reslutFuture
          .onSuccess(
              result -> {
                if (result != null) {
                  successFun.accept(result.bodyAsString());
                }
              })
          .onFailure(
              err -> {
                if (failFun != null) {
                  failFun.accept(new RuntimeException(err));
                }
              });
		} catch (Exception e) {
			e.printStackTrace();
			if (failFun != null){
				failFun.accept(e);
			}
			throw e;
		}
	}

	public static void sendForm() {

		WebClientOptions options = new WebClientOptions().setUserAgent("My-App/1.2.3");
		options.setKeepAlive(false);
		WebClient webClient = WebClient.create(vertx, options);
		MultiMap multiMap = MultiMap.caseInsensitiveMultiMap();
		multiMap.add("name", "yitian");
		multiMap.add("age", "25");
		webClient.post("httpbin.org", "/post").sendForm(multiMap).onComplete(ar -> {
			if (ar.succeeded()) {
				io.vertx.ext.web.client.HttpResponse<Buffer> response = ar.result();
				System.out.println(response.body());
			} else {
				log.error("Failed to send form", ar.cause());
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
