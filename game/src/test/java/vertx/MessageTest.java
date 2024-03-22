package vertx;

import cn.game.core.net.vertx.VxHolder;
import cn.game.protocol.protobuf.ServerMsg.GameTestRequest_7d000500;
import io.vertx.core.Future;

public class MessageTest {

	public static void main(String[] args) throws Exception {
		VxHolder.init();
//		testProtobufMessageFutrue();
		testProtobufMessageCallback();
	}

	public static void testProtobufMessageFutrue() {

		Future<io.vertx.core.eventbus.Message<Object>> future = VxHolder.requestRemoteServer("GAME_SYQ", GameTestRequest_7d000500
				.newBuilder().setId(100).build());

		future.onSuccess(r -> {
			Object body = r.body();
			System.out.println(body);
		}).onFailure(r -> {
			System.err.println(r);
		});
	}
	public static void testProtobufMessageCallback() {

		VxHolder.requestRemoteServer("GAME_SYQ", GameTestRequest_7d000500.newBuilder().setId(100).build(), r -> {
			if (r.succeeded()) {
				System.out.println(r.result().body());
			} else {
				System.err.println(r.cause());
			}
		});
	}


}
