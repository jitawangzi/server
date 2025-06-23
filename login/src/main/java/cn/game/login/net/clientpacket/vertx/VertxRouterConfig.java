package cn.game.login.net.clientpacket.vertx;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.game.core.net.vertx.VxHolder;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;

@Configuration
public class VertxRouterConfig {

	private static final Logger logger = LoggerFactory.getLogger(VertxRouterConfig.class);

	@Autowired
	private List<BaseVertxHandler> handlers;

	@Bean
	public Router configRouter() {
		Router router = Router.router(VxHolder.vertx);

		// ==================== 全局中间件配置 ====================
		// 1. 请求生命周期日志（第一个处理器）
		router.route().handler(createRequestLogger());

		// 2. 安全过滤器
		router.route().handler(new SecurityHandler());

		// 3. Body处理器
		router.route()
				.handler(BodyHandler.create()
						.setHandleFileUploads(false)
						.setMergeFormAttributes(false)
						.setDeleteUploadedFilesOnEnd(true)
						.setBodyLimit(1024 * 1024)); // 1MB限制

		// 4. 后置Body解析验证
		router.route().handler(createBodyValidationHandler());

		// ==================== 功能中间件 ====================
		configureCors(router);
		configureStatic(router);

		// ==================== 业务路由注册 ====================
		registerHandlers(router);

		// ==================== 错误处理 ====================
		router.route().failureHandler(createErrorHandler());

		return router;
	}

	// ==================== 中间件构造方法 ====================
	private Handler<RoutingContext> createRequestLogger() {
		return ctx -> {
			long startTime = System.currentTimeMillis();
			String requestId = UUID.randomUUID().toString();

			// 将requestId存入上下文
			ctx.put("requestId", requestId);

			// 记录请求开始
			logger.info("[{}] {} {} from {}", requestId, ctx.request().method(), ctx.request().path(), ctx.request().remoteAddress());

			// 响应结束回调
			ctx.response().endHandler(v -> {
				long duration = System.currentTimeMillis() - startTime;
				logger.info("[{}] Response {} ({} ms) [Size: {}b]", requestId, ctx.response().getStatusCode(), duration,
						ctx.response().bytesWritten());
			});

			ctx.next();
		};
	}

	private Handler<RoutingContext> createBodyValidationHandler() {
		return ctx -> {
			// 记录Body信息
			if (ctx.body() != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("[{}] Body received: {} bytes", ctx.get("requestId"), ctx.body().length());
				}
			}
			ctx.next();
		};
	}

	private Handler<RoutingContext> createErrorHandler() {
		return ctx -> {
			Throwable error = ctx.failure();
			String requestId = ctx.get("requestId");

			// 记录完整错误堆栈
			logger.error("[{}] Request failed: {} {}", requestId, ctx.statusCode(), error.getMessage(), error);

			ctx.response()
					.setStatusCode(ctx.statusCode() > 0 ? ctx.statusCode() : 500)
					.putHeader("Content-Type", "application/json")
					.end(new JsonObject().put("requestId", requestId).put("error", error.getMessage()).encode());
		};
	}

	// ==================== 路由注册方法 ====================
	private void registerHandlers(Router router) {
		for (BaseVertxHandler handler : handlers) {
			// 优先使用注解方式
			VertxRoute route = handler.getClass().getAnnotation(VertxRoute.class);
			if (route != null) {
				// 注解方式注册路由
				registerAnnotationRoute(router, route, handler);
			} else if (handler.getPath() != null) {
				// 接口方式注册路由
				registerInterfaceRoute(router, handler);
			} else {
				throw new IllegalArgumentException("Handler配置错误: " + handler.getClass().getName());
			}
		}
	}

	private void registerAnnotationRoute(Router router, VertxRoute route, Handler<RoutingContext> handler) {
		logger.info("注册路由: {} {} -> {}", route.method(), route.value(), handler.getClass().getSimpleName());

//		router.route(route.value()).method(HttpMethod.valueOf(route.method())).handler(handler);
		router.route(route.value()).handler(handler);
	}

	private void registerInterfaceRoute(Router router, BaseVertxHandler handler) {
		logger.info("注册路由: {} -> {}", handler.getPath(), handler.getClass().getSimpleName());
		router.route(handler.getPath()).handler(handler);
	}

	// ==================== 静态资源配置 ====================
	private void configureStatic(Router router) {

		StaticHandler staticHandler = StaticHandler.create()
//				.setAllowRootFileSystemAccess(false)
				.setCachingEnabled(true)
				.setFilesReadOnly(true)
				.setMaxAgeSeconds(24 * 60 * 60) // 24小时缓存
				.setDirectoryListing(false); // 禁止目录浏览

		router.route("/*").handler(staticHandler);

		logger.info("静态资源路由已配置");
	}

	// ==================== CORS配置 ====================
	private void configureCors(Router router) {

		Set<String> allowedHeaders = new HashSet<>();
		allowedHeaders.add("x-requested-with");
		allowedHeaders.add("Access-Control-Allow-Origin");
		allowedHeaders.add("origin");
		allowedHeaders.add("Content-Type");
		allowedHeaders.add("accept");

		Set<HttpMethod> allowedMethods = new HashSet<>();
		allowedMethods.add(HttpMethod.GET);
		allowedMethods.add(HttpMethod.POST);
		allowedMethods.add(HttpMethod.OPTIONS);

		router.route().handler(CorsHandler.create().allowedHeaders(allowedHeaders).allowedMethods(allowedMethods));

		logger.info("CORS策略已配置");
	}
}

// ==================== 安全处理器增强版 ====================
class SecurityHandler implements Handler<RoutingContext> {
	private static final Logger logger = LoggerFactory.getLogger(SecurityHandler.class);

	@Override
	public void handle(RoutingContext ctx) {
		try {
			String path = URLDecoder.decode(ctx.request().path(), StandardCharsets.UTF_8);
			String requestId = ctx.get("requestId");

			// 路径安全检查
			if (isInvalidPath(path)) {
				logger.warn("[{}] 拦截危险路径: {}", requestId, path);
				sendBlockResponse(ctx, 400, "Invalid path");
				return;
			}

			// HTTPS重定向检查（可选）
			if (!ctx.request().isSSL() && "http".equals(ctx.request().scheme())) {
				logger.info("[{}] 非安全HTTP请求", requestId);
				// 这里可以添加重定向逻辑
			}

			ctx.next();
		} catch (Exception e) {
			logger.error("安全检测异常: {}", e.getMessage(), e);
			sendBlockResponse(ctx, 500, "Security check failed");
		}
	}

	private boolean isInvalidPath(String decodedPath) {
		return decodedPath.contains("../") || decodedPath.contains("..\\") || decodedPath.contains("%2e%2e%2f")
				|| decodedPath.contains("%2e%2e/") || decodedPath.contains("..%2f") || !decodedPath.matches("^[a-zA-Z0-9/._-]+$");
	}

	private void sendBlockResponse(RoutingContext ctx, int code, String message) {
		ctx.response()
				.setStatusCode(code)
				.putHeader("Content-Type", "application/json")
				.end(new JsonObject().put("requestId", ctx.get("requestId")).put("error", message).encode());
	}
}