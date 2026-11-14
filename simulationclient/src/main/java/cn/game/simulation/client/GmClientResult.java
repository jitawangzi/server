package cn.game.simulation.client;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.game.protocol.protobuf.TestMsg.TestGmExecResponse_6f000103;

/**
 * 模拟客户端结构化GM调用返回结果模型
 *
 * @author shuyongqiang
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GmClientResult {

	/** 日志记录器 */
	private static final Logger log = LoggerFactory.getLogger(GmClientResult.class);
	/** 响应JSON解析器 */
	private static final ObjectMapper MAPPER = new ObjectMapper();

	/** 协议结果结构版本号 */
	private String schemaVersion;
	/** GM执行是否成功 */
	private boolean ok;
	/** 错误码，0表示成功，-1表示本地响应JSON解析异常 */
	private int errorCode;
	/** 错误名称枚举或本地解析标识 */
	private String errorName;
	/** 执行结果摘要说明 */
	private String summary;
	/** 结构化业务返回数据 */
	private Object data;
	/** 警告信息列表 */
	private List<String> warnings = Collections.emptyList();
	/** 全链路追踪ID */
	private String traceId;
	/** 服务端执行耗时（毫秒） */
	private long elapsedMs;
	/** 目标玩家ID */
	private long playerId;
	/** 请求协议号 */
	private int msgId;
	/** 请求协议名称 */
	private String msgName;
	/** 请求消息序号 */
	private int seq;
	/** 响应时间戳（毫秒） */
	private long timestampMs;
	/** 服务端原始result_json文本 */
	private String rawResultJson;

	/**
	 * 从0x6f000103响应消息解析结构化GM执行结果
	 *
	 * @param response 服务端返回的TestGmExecResponse_6f000103
	 * @return 解析后的GmClientResult对象
	 */
	public static GmClientResult fromResponse(TestGmExecResponse_6f000103 response) {
		if (response == null) {
			throw new IllegalArgumentException("TestGmExecResponse_6f000103 cannot be null");
		}
		GmClientResult result = null;
		String json = response.getResultJson();
		if (json != null && !json.isBlank()) {
			try {
				result = MAPPER.readValue(json, GmClientResult.class);
			} catch (Exception e) {
				log.warn("解析TestGmExecResponse result_json失败 raw={}", json, e);
			}
		}
		if (result == null) {
			result = new GmClientResult();
			result.setOk(false);
			result.setErrorCode(-1);
			result.setErrorName("INVALID_RESULT_JSON");
			result.setSummary("服务端result_json为空或不可解析");
		}
		if (result.getTraceId() == null || result.getTraceId().isBlank()) {
			result.setTraceId(response.getTraceId());
		}
		result.setPlayerId(response.getPlayerId());
		result.setMsgId(response.getMsgId());
		result.setMsgName(response.getMsgName());
		result.setSeq(response.getSeq());
		result.setTimestampMs(response.getTimestampMs());
		result.setRawResultJson(json);
		return result;
	}

	/**
	 * 获取协议结果结构版本号
	 *
	 * @return 结构版本号
	 */
	public String getSchemaVersion() {
		return schemaVersion;
	}

	/**
	 * 设置协议结果结构版本号
	 *
	 * @param schemaVersion 结构版本号
	 */
	public void setSchemaVersion(String schemaVersion) {
		this.schemaVersion = schemaVersion;
	}

	/**
	 * 判断GM执行是否成功
	 *
	 * @return true表示成功
	 */
	public boolean isOk() {
		return ok;
	}

	/**
	 * 设置GM执行成功标志
	 *
	 * @param ok 是否成功
	 */
	public void setOk(boolean ok) {
		this.ok = ok;
	}

	/**
	 * 获取错误码
	 *
	 * @return 错误码
	 */
	public int getErrorCode() {
		return errorCode;
	}

	/**
	 * 设置错误码
	 *
	 * @param errorCode 错误码
	 */
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * 获取错误名称
	 *
	 * @return 错误名称
	 */
	public String getErrorName() {
		return errorName;
	}

	/**
	 * 设置错误名称
	 *
	 * @param errorName 错误名称
	 */
	public void setErrorName(String errorName) {
		this.errorName = errorName;
	}

	/**
	 * 获取执行结果摘要
	 *
	 * @return 结果摘要
	 */
	public String getSummary() {
		return summary;
	}

	/**
	 * 设置执行结果摘要
	 *
	 * @param summary 结果摘要
	 */
	public void setSummary(String summary) {
		this.summary = summary;
	}

	/**
	 * 获取结构化返回数据
	 *
	 * @return 返回数据对象
	 */
	public Object getData() {
		return data;
	}

	/**
	 * 设置结构化返回数据
	 *
	 * @param data 返回数据对象
	 */
	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * 获取警告信息列表
	 *
	 * @return 警告列表
	 */
	public List<String> getWarnings() {
		return warnings;
	}

	/**
	 * 设置警告信息列表（传入null时自动归为空列表）
	 *
	 * @param warnings 警告列表
	 */
	public void setWarnings(List<String> warnings) {
		this.warnings = warnings != null ? warnings : Collections.emptyList();
	}

	/**
	 * 获取全链路追踪ID
	 *
	 * @return 追踪ID
	 */
	public String getTraceId() {
		return traceId;
	}

	/**
	 * 设置全链路追踪ID
	 *
	 * @param traceId 追踪ID
	 */
	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	/**
	 * 获取服务端执行耗时（毫秒）
	 *
	 * @return 执行耗时毫秒数
	 */
	public long getElapsedMs() {
		return elapsedMs;
	}

	/**
	 * 设置服务端执行耗时（毫秒）
	 *
	 * @param elapsedMs 执行耗时毫秒数
	 */
	public void setElapsedMs(long elapsedMs) {
		this.elapsedMs = elapsedMs;
	}

	/**
	 * 获取目标玩家ID
	 *
	 * @return 玩家ID
	 */
	public long getPlayerId() {
		return playerId;
	}

	/**
	 * 设置目标玩家ID
	 *
	 * @param playerId 玩家ID
	 */
	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	/**
	 * 获取请求协议号
	 *
	 * @return 协议号
	 */
	public int getMsgId() {
		return msgId;
	}

	/**
	 * 设置请求协议号
	 *
	 * @param msgId 协议号
	 */
	public void setMsgId(int msgId) {
		this.msgId = msgId;
	}

	/**
	 * 获取请求协议名称
	 *
	 * @return 协议名称
	 */
	public String getMsgName() {
		return msgName;
	}

	/**
	 * 设置请求协议名称
	 *
	 * @param msgName 协议名称
	 */
	public void setMsgName(String msgName) {
		this.msgName = msgName;
	}

	/**
	 * 获取请求消息序号
	 *
	 * @return 消息序号
	 */
	public int getSeq() {
		return seq;
	}

	/**
	 * 设置请求消息序号
	 *
	 * @param seq 消息序号
	 */
	public void setSeq(int seq) {
		this.seq = seq;
	}

	/**
	 * 获取响应时间戳（毫秒）
	 *
	 * @return 时间戳毫秒值
	 */
	public long getTimestampMs() {
		return timestampMs;
	}

	/**
	 * 设置响应时间戳（毫秒）
	 *
	 * @param timestampMs 时间戳毫秒值
	 */
	public void setTimestampMs(long timestampMs) {
		this.timestampMs = timestampMs;
	}

	/**
	 * 获取服务端原始result_json文本
	 *
	 * @return 原始JSON字符串
	 */
	public String getRawResultJson() {
		return rawResultJson;
	}

	/**
	 * 设置服务端原始result_json文本
	 *
	 * @param rawResultJson 原始JSON字符串
	 */
	public void setRawResultJson(String rawResultJson) {
		this.rawResultJson = rawResultJson;
	}
}