package vertx;

import cn.game.core.net.vertx.VxHolder;
import cn.game.protocol.protobuf.ServerMsg.GameTestRequest_7d000500;
import cn.game.protocol.protobuf.ServerMsg.ServerStatusRequest_7d000901;
import cn.game.protocol.protobuf.ServerMsg.ServerStatusResponse_7d000902;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;

public class MessageTest {

	public static void main(String[] args) throws Exception {
//		ServerContext.getInstance().init(ServerType.Game, "ddddd");
//		VxHolder.init();
//		testProtobufMessageFutrue();
//		testProtobufMessageCallback();

		testRequestGameSereverCallback();

	}


	private static void testRequestGameSereverCallback() {
		DeliveryOptions options = new DeliveryOptions(VxHolder.universalOptions);
		options.setSendTimeout(3000);
		Future<Message<ServerStatusResponse_7d000902>> future = VxHolder.vertx.eventBus().request("game_test",
				ServerStatusRequest_7d000901.getDefaultInstance(), options);
		future.onComplete(r -> {
			if (r.cause()!=null) {
				System.out.println(r.cause());
			}else {
				Message<ServerStatusResponse_7d000902> result = r.result(); 
				System.out.println(result);
			}
		});
	}


	public static void testProtobufMessageFutrue() {

		Future<Object> future = VxHolder.requestRemoteServer("GAME_SYQ", GameTestRequest_7d000500
				.newBuilder().setId(100).build());

		future.onSuccess(r -> {
			Object body = r;
			System.out.println(body);
		}).onFailure(r -> {
			System.err.println(r);
		});
	}

}
