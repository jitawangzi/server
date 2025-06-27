package performance;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.game.protocol.protobuf.Account.AccountLoginResponse;
import cn.game.protocol.protobuf.Account.HttpResult;


public class Test {

	private static ExecutorService executorService = Executors.newFixedThreadPool(100);
	public static final byte[] REQ_RESP = new byte[] { 1 };

	public static void main(String[] args) throws Exception {

//		PlayerLoginRequest_01000001 playerLoginRequest_01000001 = PlayerLoginRequest_01000001.newBuilder()
//				.setSessionId(UUID.randomUUID().toString()).build();
//
//		Gson gson = new Gson();
//		String json = gson.toJson(playerLoginRequest_01000001);
		

//		System.out.println("序列化后的json : " + json);
//		
//		json = "{\"sessionId\":\"ebbeeaf0-1f01-4fdc-81a3-e672ff6d0ba5\",\"reconnect_\":false,\"memoizedIsInitialized\":1,\"unknownFields\":{\"fields\":{},\"fieldsDescending\":{}},\"memoizedSize\":-1,\"memoizedHashCode\":0}";
//		String json_ = "{\"sessionId_\":\"ebbeeaf0-1f01-4fdc-81a3-e672ff6d0ba5\",\"reconnect_\":false,\"memoizedIsInitialized\":1,\"unknownFields\":{\"fields\":{},\"fieldsDescending\":{}},\"memoizedSize\":-1,\"memoizedHashCode\":0}";
//
//		PlayerLoginRequest_01000001 obj = gson.fromJson(json, PlayerLoginRequest_01000001.class);
//		System.out.println("反序列化后的json对象 : " + obj);
//
//		PlayerLoginRequest_01000001 obj_ = gson.fromJson(json_, PlayerLoginRequest_01000001.class);
//		System.out.println("反序列化后的json_对象 : " + obj_);


		HttpResult httpResult = HttpResult.newBuilder().setErrorMsg("账号不存在").build();
		byte[] byteArray = AccountLoginResponse.newBuilder().setResult(httpResult).build().toByteArray();

//		byte[] byteArray = httpResult.toByteArray();
//		
//		HttpResult from = HttpResult.parseFrom(byteArray);
		AccountLoginResponse from = AccountLoginResponse.parseFrom(byteArray);
		System.out.println(from.getResult().getErrorMsg());

	}
}
