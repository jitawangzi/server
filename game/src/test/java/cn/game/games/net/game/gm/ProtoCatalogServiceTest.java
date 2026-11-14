package cn.game.games.net.game.gm;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;

import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;

/**
 * 协议与错误码目录服务单元测试
 *
 * @author shuyongqiang
 */
public class ProtoCatalogServiceTest {

	@Test
	void verifyProtocolPairingSchemasAndErrorDictionary() throws Exception {
		// 构造自引用递归消息验证 depth <= 8 截断保护
		DescriptorProto recursiveMsg = DescriptorProto.newBuilder()
				.setName("RecursiveNode")
				.addField(FieldDescriptorProto.newBuilder()
						.setName("value")
						.setNumber(1)
						.setType(FieldDescriptorProto.Type.TYPE_INT32)
						.setLabel(FieldDescriptorProto.Label.LABEL_OPTIONAL))
				.addField(FieldDescriptorProto.newBuilder()
						.setName("next")
						.setNumber(2)
						.setType(FieldDescriptorProto.Type.TYPE_MESSAGE)
						.setTypeName("RecursiveNode")
						.setLabel(FieldDescriptorProto.Label.LABEL_OPTIONAL))
				.build();
		FileDescriptorProto fileProto = FileDescriptorProto.newBuilder()
				.setName("recursive_test.proto")
				.addMessageType(recursiveMsg)
				.build();
		FileDescriptor fileDesc = FileDescriptor.buildFrom(fileProto, new FileDescriptor[0]);
		Descriptor recursiveDesc = fileDesc.findMessageTypeByName("RecursiveNode");

		Map<String, Object> schema = ProtoCatalogService.describeMessage(recursiveDesc, Map.of(), 0);
		Assertions.assertNotNull(schema);
		Map<String, Object> cursor = schema;
		int depth = 0;
		boolean sawTruncated = false;
		while (cursor != null && depth < 16) {
			if (Boolean.TRUE.equals(cursor.get("truncated"))) {
				sawTruncated = true;
				break;
			}
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> fields = (List<Map<String, Object>>) cursor.get("fields");
			if (fields == null) {
				break;
			}
			Map<String, Object> nextField = null;
			for (Map<String, Object> f : fields) {
				if ("next".equals(f.get("name"))) {
					nextField = f;
					break;
				}
			}
			if (nextField == null) {
				break;
			}
			@SuppressWarnings("unchecked")
			Map<String, Object> nested = (Map<String, Object>) nextField.get("nestedSchema");
			cursor = nested;
			depth++;
		}
		Assertions.assertTrue(sawTruncated, "递归消息超过上限深度 8 时必须返回 truncated 标记");
		Assertions.assertTrue(depth <= 8, "递归深度不得超过 8");

		// 严格 Request + 1 = Response 配对校验（非 Response 名称不得错误配对）
		Map<Integer, String> idNames = new java.util.LinkedHashMap<>();
		idNames.put(0x01000001, "SampleLoginRequest_01000001");
		idNames.put(0x01000002, "SampleLoginResponse_01000002");
		idNames.put(0x01000003, "SampleUnpairedRequest_01000003");
		idNames.put(0x01000004, "SampleNotifyPush_01000004");

		Assertions.assertEquals(0x01000002, ProtoCatalogService.resolvePairedResponseId(0x01000001, idNames));
		Assertions.assertNull(ProtoCatalogService.resolvePairedResponseId(0x01000003, idNames),
				"reqId+1 名称不含 Response 时必须返回 null");

		// 无源码路径时注释平稳降级为空串，真实 PbProtocol 目录可正常枚举
		ProtoCatalogService fallbackService = new ProtoCatalogService(Path.of("Z:/non-existent-proto-dir-for-test"));
		ProtoGm fallbackProtoGm = new ProtoGm(fallbackService);

		Map<String, Object> fallbackCatalog = fallbackProtoGm.catalog(null, null);
		Assertions.assertEquals("1.0", fallbackCatalog.get("schemaVersion"));
		Assertions.assertInstanceOf(Map.class, fallbackCatalog.get("modules"));
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> fallbackEntries = (List<Map<String, Object>>) fallbackCatalog.get("entries");
		Assertions.assertFalse(fallbackEntries.isEmpty(), "协议目录不应为空");
		Assertions.assertTrue(fallbackEntries.stream().anyMatch(
				e -> Integer.valueOf(PbProtocol.GmPlayerRequest_77000021).equals(e.get("reqId"))
						&& Integer.valueOf(PbProtocol.GmPlayerResponse_77000022).equals(e.get("respId"))));
		Assertions.assertEquals("", fallbackEntries.get(0).get("comment"), "无源码部署时注释应平稳降级为空串");

		// 默认真实 protocol/src/main/proto 路径可正确提取包名与中文消息注释
		ProtoCatalogService realService = new ProtoCatalogService();
		Map<String, Object> gmCatalog = realService.listCatalog("0x77", "GmPlayerRequest_77000021");
		Assertions.assertEquals("1.0", gmCatalog.get("schemaVersion"));
		@SuppressWarnings("unchecked")
		Map<String, List<Map<String, Object>>> modules = (Map<String, List<Map<String, Object>>>) gmCatalog.get("modules");
		Assertions.assertTrue(modules.containsKey("0x77"));
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> gmEntries = (List<Map<String, Object>>) gmCatalog.get("entries");
		Map<String, Object> gmPlayerEntry = gmEntries.stream()
				.filter(e -> Integer.valueOf(PbProtocol.GmPlayerRequest_77000021).equals(e.get("reqId")))
				.findFirst()
				.orElseThrow();
		String realComment = (String) gmPlayerEntry.get("comment");
		Assertions.assertNotNull(realComment);
		Assertions.assertFalse(realComment.isBlank(), "真实proto源码应提取到GmPlayerRequest_77000021中文注释");

		// ErrorMsgEnum 错误码字典完整性与 disconnect 字段校验
		Map<String, Object> errors = fallbackProtoGm.errors(null);
		Assertions.assertEquals("1.0", errors.get("schemaVersion"));
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> errList = (List<Map<String, Object>>) errors.get("errors");
		Assertions.assertEquals(ErrorMsgEnum.values().length, errList.size());
		Map<String, Object> notFoundErr = errList.stream()
				.filter(e -> Integer.valueOf(ErrorMsgEnum.config_data_not_found.getId()).equals(e.get("id")))
				.findFirst()
				.orElseThrow();
		Assertions.assertEquals("config_data_not_found", notFoundErr.get("name"));
		Assertions.assertEquals(ErrorMsgEnum.config_data_not_found.isNeedProcessed(), notFoundErr.get("disconnect"));
	}
}