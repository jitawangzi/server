package cn.game.login.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.impl.HttpServerImpl;
import io.vertx.core.impl.VertxInternal;

public class LoginVerticle extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		vertx.createHttpServer().requestHandler(req -> {
			req.response().end("hello Vert.x");
		}).listen(8888);
	}

	public HttpServer createHttpServer(HttpServerOptions serverOptions) {
		return new HttpServerImpl((VertxInternal) this, serverOptions);
	}
}
