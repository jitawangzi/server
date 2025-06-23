
import java.util.LinkedList;
import java.util.Queue;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;

public class WebSocketServer {

	private static final int MAX_QUEUE_SIZE = 100; // 设置任务队列的最大大小

	public static void main(String[] args) {
		WebSocketServer server = new WebSocketServer();
		server.start();
	}

	public void start() {
		Vertx vertx = Vertx.vertx();
		HttpServer server = vertx.createHttpServer();

		server.webSocketHandler(ws -> {
			Queue<Buffer> messageQueue = new LinkedList<>();

			ws.binaryMessageHandler(buffer -> {
				if (messageQueue.size() < MAX_QUEUE_SIZE) {
					messageQueue.add(buffer);
				} else {
					// 当队列满时拒绝新的消息
					System.out.println("Queue is full, rejecting message");
					ws.writeFinalTextFrame("Service Unavailable: too many requests");
				}
				processMessages(ws, messageQueue);
			});

			ws.textMessageHandler(r -> {
				System.out.println("Not supported: text message " + r);
			});

			ws.closeHandler(v -> {
				System.out.println("Connection closed");
			});

			ws.exceptionHandler(e -> {
				e.printStackTrace();
				ws.close();
			});

			// 启动时暂停读取，等到缓冲区有空间再恢复读取
			ws.pause();
		});

		server.listen(8080).onComplete(res -> {
			if (res.succeeded()) {
				System.out.println("WebSocket server is listening on port 8080");
			} else {
				System.err.println("Failed to start WebSocket server: " + res.cause().getMessage());
			}
		});
	}

	private void processMessages(ServerWebSocket ws, Queue<Buffer> messageQueue) {
		while (!messageQueue.isEmpty()) {
			Buffer buffer = messageQueue.poll();
			// 处理二进制消息的逻辑
			System.out.println("Processing binary message: " + buffer.toString());
			// 模拟处理时间
			try {
				Thread.sleep(100); // 模拟处理时间
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		if (messageQueue.isEmpty()) {
			ws.resume(); // 当缓冲区有空间时恢复读取
		} else {
			ws.pause();
		}
	}
}
