package cn.game.games.net.game.gm.proto;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import cn.game.protocol.protobuf.BaseMsg.ItemInfo;
import cn.game.protocol.protobuf.MailMsg.MailListFilter;
import cn.game.protocol.protobuf.MailMsg.MailListRequest_12000001;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.PlayerMsg.PlayerNameRequest_01000011;

/**
 * Protobuf JSON 编解码器单元测试 (TC-04-PROTO-JSON-CODEC, DC-03, DR-02)
 *
 * @author shuyongqiang
 */
public class ProtoJsonCodecTest {

	@Test
	public void testBidirectionalJsonCodecAndValidation() {
		// 1. 简单消息转换: PlayerNameRequest_01000011
		int nameReqId = PbProtocol.getInstance().getMsgId("PlayerNameRequest_01000011");
		Assertions.assertTrue(nameReqId > 0);

		Map<String, Object> namePayload = new LinkedHashMap<>();
		namePayload.put("name", "Alice_Test");

		PlayerNameRequest_01000011 nameMsg = (PlayerNameRequest_01000011) ProtoJsonCodec.fromJson(
				getParser(nameReqId),
				getDescriptor(nameReqId),
				namePayload);
		Assertions.assertEquals("Alice_Test", nameMsg.getName());

		Map<String, Object> nameMap = ProtoJsonCodec.toMap(nameMsg);
		Assertions.assertEquals("Alice_Test", nameMap.get("name"));

		// 2. 枚举与可选字段: MailListRequest_12000001
		int mailListReqId = PbProtocol.getInstance().getMsgId("MailListRequest_12000001");
		Assertions.assertTrue(mailListReqId > 0);

		// (a) 枚举名字符串转换
		Map<String, Object> mailPayload = new LinkedHashMap<>();
		mailPayload.put("filter", "UNREAD");
		mailPayload.put("page_size", 20);
		mailPayload.put("cursor", "cur_123");

		MailListRequest_12000001 mailMsg = (MailListRequest_12000001) ProtoJsonCodec.fromJson(
				getParser(mailListReqId),
				getDescriptor(mailListReqId),
				mailPayload);
		Assertions.assertEquals(MailListFilter.UNREAD, mailMsg.getFilter());
		Assertions.assertEquals(20, mailMsg.getPageSize());
		Assertions.assertEquals("cur_123", mailMsg.getCursor());

		// (b) camelCase (jsonName) 兼容
		Map<String, Object> camelPayload = new LinkedHashMap<>();
		camelPayload.put("pageSize", 50);
		MailListRequest_12000001 camelMsg = (MailListRequest_12000001) ProtoJsonCodec.fromJson(
				getParser(mailListReqId),
				getDescriptor(mailListReqId),
				camelPayload);
		Assertions.assertEquals(50, camelMsg.getPageSize());

		// (c) 未知字段校验报错
		Map<String, Object> invalidPayload = new LinkedHashMap<>();
		invalidPayload.put("unknown_field_foo", "bar");
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			ProtoJsonCodec.fromJson(
					getParser(mailListReqId),
					getDescriptor(mailListReqId),
					invalidPayload);
		});

		// (d) 数值越界报错
		Map<String, Object> overflowPayload = new LinkedHashMap<>();
		overflowPayload.put("page_size", 9999999999999L);
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			ProtoJsonCodec.fromJson(
					getParser(mailListReqId),
					getDescriptor(mailListReqId),
					overflowPayload);
		});

		// 3. 复杂结构 ItemInfo: 标量、时间戳
		Map<String, Object> itemPayload = new LinkedHashMap<>();
		itemPayload.put("id", 1001);
		itemPayload.put("count", 99L);
		ItemInfo itemMsg = (ItemInfo) ProtoJsonCodec.fromJson(
				ItemInfo.parser(),
				ItemInfo.getDescriptor(),
				itemPayload);
		Assertions.assertEquals(1001, itemMsg.getId());
		Assertions.assertEquals(99L, itemMsg.getCount());

		// 4. 空载荷与字符串 JSON 解析
		PlayerNameRequest_01000011 emptyMsg = (PlayerNameRequest_01000011) ProtoJsonCodec.fromJson(
				getParser(nameReqId),
				getDescriptor(nameReqId),
				"{}");
		Assertions.assertEquals("", emptyMsg.getName());
	}

	private static com.google.protobuf.Parser<?> getParser(int msgId) {
		return PbProtocol.parsersMap.get(msgId);
	}

	private static com.google.protobuf.Descriptors.Descriptor getDescriptor(int msgId) {
		return cn.game.games.net.game.gm.ProtoCatalogService.resolveDescriptor(PbProtocol.parsersMap.get(msgId));
	}
}
