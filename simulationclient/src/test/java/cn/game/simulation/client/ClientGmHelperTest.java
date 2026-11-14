package cn.game.simulation.client;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.protobuf.Message;

import cn.game.core.exception.LogicException;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerErrorPush_01000099;
import cn.game.protocol.protobuf.TestMsg.TestGmExecRequest_6f000102;
import cn.game.protocol.protobuf.TestMsg.TestGmExecResponse_6f000103;
import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;

/**
 * simulationclient最小通用辅助Client#gm协议封装、结果解析与失败快速返回单元测试
 *
 * @author shuyongqiang
 */
public class ClientGmHelperTest {

	/**
	 * 本地回环模拟客户端，用于验证0x6f000102单包往返与错误透传
	 *
	 * @author shuyongqiang
	 */
	static final class LoopbackGmClient extends Client {
		final List<Message> sentMessages = new ArrayList<>();
		private int nextSeq = 100;

		LoopbackGmClient() {
			super("gm-helper-test");
			setPlayerId(99001L);
		}

		@Override
		public ChannelFuture sendProtocol(Message message) {
			sentMessages.add(message);
			TestGmExecRequest_6f000102 req = (TestGmExecRequest_6f000102) message;
			int seq = nextSeq++;
			this.lastSendMessageId = PbProtocol.TestGmExecRequest_6f000102;

			String resultJson;
			if ("bad.action".equals(req.getAction())) {
				resultJson = "{\"schemaVersion\":\"1.0\",\"ok\":false,\"errorCode\":19,\"errorName\":\"gm_cmd_param\",\"summary\":\"参数错误\",\"traceId\":\""
						+ req.getTraceId() + "\",\"elapsedMs\":2}";
			} else {
				resultJson = "{\"schemaVersion\":\"1.0\",\"ok\":true,\"errorCode\":0,\"summary\":\"执行成功:"
						+ req.getAction() + "\",\"data\":{\"action\":\"" + req.getAction() + "\"},\"traceId\":\""
						+ req.getTraceId() + "\",\"elapsedMs\":3}";
			}
			TestGmExecResponse_6f000103 resp = TestGmExecResponse_6f000103.newBuilder()
					.setResultJson(resultJson)
					.setPlayerId(getPlayerId())
					.setMsgId(PbProtocol.TestGmExecRequest_6f000102)
					.setMsgName("TestGmExecRequest_6f000102")
					.setSeq(seq)
					.setTimestampMs(1700000000000L)
					.setTraceId(req.getTraceId())
					.build();
			onResponse(PbProtocol.TestGmExecResponse_6f000103, seq, 0, resp);
			return null;
		}
	}

	/**
	 * 验证Client#gm正常往返、ServerTestContext#gm与playerAllInfo零副作用
	 *
	 * @throws Exception 反射访问异常
	 */
	@Test
	void verifyClientGmHelperRoundTrip() throws Exception {
		// entryPointIds: MUTATE:TestGmExecRequest_6f000102, variantKeys: structuredGmExec
		LoopbackGmClient client = new LoopbackGmClient();
		PlayerAllInfo initialAllInfo = PlayerAllInfo.newBuilder().build();
		Field allInfoField = Client.class.getDeclaredField("playerAllInfo");
		allInfoField.setAccessible(true);
		allInfoField.set(client, initialAllInfo);

		GmClientResult schemaResult = client.gm("gm.schema");
		Assertions.assertTrue(schemaResult.isOk());
		Assertions.assertEquals("1.0", schemaResult.getSchemaVersion());
		Assertions.assertEquals(PbProtocol.TestGmExecRequest_6f000102, schemaResult.getMsgId());
		Assertions.assertEquals("TestGmExecRequest_6f000102", schemaResult.getMsgName());
		Assertions.assertEquals(99001L, schemaResult.getPlayerId());

		GmClientResult configGetResult = client.gm("config.get", "{\"table\":\"Item\",\"id\":100001}");
		Assertions.assertTrue(configGetResult.isOk());
		Assertions.assertTrue(configGetResult.getSummary().contains("config.get"));

		GmClientResult inspectResult = client.gm("gm.inspect", Map.of("modules", "MailModule", "scope", "player"));
		Assertions.assertTrue(inspectResult.isOk());

		GmClientResult failResult = ServerTestContext.gm(client, "bad.action", "{}");
		Assertions.assertFalse(failResult.isOk());
		Assertions.assertEquals(19, failResult.getErrorCode());
		Assertions.assertEquals("gm_cmd_param", failResult.getErrorName());

		Assertions.assertEquals(4, client.sentMessages.size(), "每次gm()调用仅应发送1条0x6f000102请求，不应隐式追加同步协议");
		for (Message msg : client.sentMessages) {
			Assertions.assertTrue(msg instanceof TestGmExecRequest_6f000102);
		}
		Assertions.assertSame(initialAllInfo, client.getPlayerAllInfo(), "gm()调用不应篡改Client.playerAllInfo缓存");
	}

	/**
	 * 验证gmOrThrow业务错误抛出LogicException、ChannelFuture失败/null快速失败、响应超时与非法result_json解析
	 */
	@Test
	void verifyClientGmFailureAndBoundaryHandling() {
		Client errorCodeClient = new Client("error-code-channel") {
			@Override
			public ChannelFuture sendProtocol(Message message) {
				this.lastSendMessageId = PbProtocol.TestGmExecRequest_6f000102;
				onResponse(PbProtocol.TestGmExecResponse_6f000103, 1, 19, TestGmExecResponse_6f000103.getDefaultInstance());
				return null;
			}
		};
		RuntimeException logicThrown = Assertions.assertThrows(RuntimeException.class,
				() -> errorCodeClient.gm("gm.schema", "{}"));
		LogicException logicEx = findCause(logicThrown, LogicException.class);
		Assertions.assertNotNull(logicEx, "服务端非0 errorCode回包应触发LogicException");
		Assertions.assertEquals(19, logicEx.getErrorCode());

		// 通道为空返回null且无回包时必须立即以IllegalStateException快速失败，不得挂起等待超时
		Client brokenChannelClient = new Client("broken-channel") {
			@Override
			public ChannelFuture sendProtocol(Message message) {
				return null;
			}
		};
		long startNs = System.nanoTime();
		RuntimeException brokenEx = Assertions.assertThrows(RuntimeException.class,
				() -> brokenChannelClient.gm("gm.schema"));
		long brokenElapsedMs = (System.nanoTime() - startNs) / 1_000_000L;
		Assertions.assertTrue(hasCause(brokenEx, IllegalStateException.class),
				"sendProtocol返回null应快速抛出IllegalStateException");
		Assertions.assertTrue(brokenElapsedMs < 1000L, "通道不可用不应干等5秒超时");

		// ChannelFuture异步失败时必须立即失败唤醒等待方
		EmbeddedChannel embeddedChannel = new EmbeddedChannel();
		Client asyncFailClient = new Client("async-fail-channel") {
			@Override
			public ChannelFuture sendProtocol(Message message) {
				DefaultChannelPromise promise = new DefaultChannelPromise(embeddedChannel);
				promise.setFailure(new java.io.IOException("simulated-write-failure"));
				return promise;
			}
		};
		RuntimeException asyncEx = Assertions.assertThrows(RuntimeException.class,
				() -> asyncFailClient.gm("gm.schema"));
		Assertions.assertTrue(hasCause(asyncEx, java.io.IOException.class),
				"ChannelFuture失败应传播IOException");
		embeddedChannel.finishAndReleaseAll();

		// 有效ChannelFuture但服务端无回包时按timeoutSeconds触发超时，且超时后滞后错误包/推送不抛IllegalStateException
		EmbeddedChannel timeoutChannel = new EmbeddedChannel();
		Client timeoutClient = new Client("timeout-channel") {
			@Override
			public ChannelFuture sendProtocol(Message message) {
				return new DefaultChannelPromise(timeoutChannel).setSuccess();
			}
		};
		RuntimeException timeoutEx = Assertions.assertThrows(RuntimeException.class,
				() -> timeoutClient.sendProtocolAndWait(TestGmExecRequest_6f000102.newBuilder().setAction("gm.schema").build(), 1));
		Assertions.assertTrue(hasCause(timeoutEx, TimeoutException.class),
				"无回包时应在超时后抛出TimeoutException");
		Assertions.assertDoesNotThrow(() -> {
			timeoutClient.onResponse(PbProtocol.TestGmExecResponse_6f000103, 1, 19,
					TestGmExecResponse_6f000103.getDefaultInstance());
			timeoutClient.onResponse(PbProtocol.PlayerErrorPush_01000099, 2, 0,
					PlayerErrorPush_01000099.getDefaultInstance());
		}, "超时清理respPromise后收到滞后错误回包或PlayerErrorPush不得抛IllegalStateException");
		timeoutChannel.finishAndReleaseAll();

		// 同时满足 id <= 0 且 errorCode > 0 时，首次tryFail后立即return，不得二次fail触发IllegalStateException
		Client badIdAndErrorClient = new Client("bad-id-and-error-channel") {
			@Override
			public ChannelFuture sendProtocol(Message message) {
				this.lastSendMessageId = PbProtocol.TestGmExecRequest_6f000102;
				onResponse(0, 1, 19, TestGmExecResponse_6f000103.getDefaultInstance());
				return null;
			}
		};
		RuntimeException badIdEx = Assertions.assertThrows(RuntimeException.class,
				() -> badIdAndErrorClient.gm("gm.schema"));
		LogicException badIdLogic = findCause(badIdEx, LogicException.class);
		Assertions.assertNotNull(badIdLogic, "id<=0且errorCode>0错包应向上传递LogicException而非IllegalStateException");
		Assertions.assertEquals(19, badIdLogic.getErrorCode());

		// 写入失败后收到滞后错误包与PlayerErrorPush，均不得抛IllegalStateException
		EmbeddedChannel failChannel = new EmbeddedChannel();
		Client lateAfterWriteFailClient = new Client("late-after-write-fail") {
			@Override
			public ChannelFuture sendProtocol(Message message) {
				DefaultChannelPromise promise = new DefaultChannelPromise(failChannel);
				promise.setFailure(new java.io.IOException("write-failed"));
				return promise;
			}
		};
		RuntimeException writeFailEx = Assertions.assertThrows(RuntimeException.class,
				() -> lateAfterWriteFailClient.gm("gm.schema"));
		Assertions.assertTrue(hasCause(writeFailEx, java.io.IOException.class));
		Assertions.assertDoesNotThrow(() -> {
			lateAfterWriteFailClient.onResponse(-1, 1, 20, TestGmExecResponse_6f000103.getDefaultInstance());
			lateAfterWriteFailClient.onResponse(PbProtocol.PlayerErrorPush_01000099, 2, 0,
					PlayerErrorPush_01000099.getDefaultInstance());
		}, "写入失败后收到滞后错误包不得抛IllegalStateException");
		failChannel.finishAndReleaseAll();

		// 服务端返回空或非法result_json时，errorCode=-1且errorName=INVALID_RESULT_JSON
		GmClientResult emptyJsonResult = GmClientResult.fromResponse(TestGmExecResponse_6f000103.newBuilder()
				.setResultJson("")
				.setTraceId("t-empty")
				.build());
		Assertions.assertFalse(emptyJsonResult.isOk());
		Assertions.assertEquals(-1, emptyJsonResult.getErrorCode());
		Assertions.assertEquals("INVALID_RESULT_JSON", emptyJsonResult.getErrorName());
		Assertions.assertEquals("t-empty", emptyJsonResult.getTraceId());

		GmClientResult malformedJsonResult = GmClientResult.fromResponse(TestGmExecResponse_6f000103.newBuilder()
				.setResultJson("{not-json")
				.build());
		Assertions.assertFalse(malformedJsonResult.isOk());
		Assertions.assertEquals(-1, malformedJsonResult.getErrorCode());
		Assertions.assertEquals("INVALID_RESULT_JSON", malformedJsonResult.getErrorName());
	}

	private static boolean hasCause(Throwable throwable, Class<? extends Throwable> expectedType) {
		return findCause(throwable, expectedType) != null;
	}

	private static <T extends Throwable> T findCause(Throwable throwable, Class<T> expectedType) {
		Throwable cur = throwable;
		while (cur != null) {
			if (expectedType.isInstance(cur)) {
				return expectedType.cast(cur);
			}
			cur = cur.getCause();
		}
		return null;
	}
}