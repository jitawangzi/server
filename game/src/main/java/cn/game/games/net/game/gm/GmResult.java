package cn.game.games.net.game.gm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import cn.game.protocol.manual.ErrorMsgEnum;

/**
 * GM统一结构化返回信封（版本1.0）
 *
 * @author shuyongqiang
 */
public class GmResult {

	public static final String SCHEMA_VERSION = "1.0";

	private String schemaVersion = SCHEMA_VERSION;
	private boolean ok;
	private int errorCode;
	private String errorName;
	private String summary;
	private List<String> warnings = new ArrayList<>();
	private Object data;
	private String traceId;
	private long elapsedMs;
	private boolean dryRun;
	@JsonInclude(JsonInclude.Include.ALWAYS)
	private Object diff;

	public GmResult() {
	}

	public static GmResult success(String summary, Object data) {
		GmResult r = new GmResult();
		r.ok = true;
		r.errorCode = ErrorMsgEnum.ok.getId();
		r.errorName = ErrorMsgEnum.ok.name();
		r.summary = summary != null ? summary : "ok";
		r.data = data;
		return r;
	}

	public static GmResult fail(ErrorMsgEnum error, String summary) {
		ErrorMsgEnum effective = error != null ? error : ErrorMsgEnum.unknown;
		String msg = summary != null && !summary.isEmpty() ? summary : effective.getDesc();
		return fail(effective.getId(), effective.name(), msg);
	}

	public static GmResult fail(int errorCode, String errorName, String summary) {
		GmResult r = new GmResult();
		r.ok = false;
		r.errorCode = errorCode;
		r.errorName = errorName != null ? errorName : String.valueOf(errorCode);
		r.summary = summary != null ? summary : "failed";
		return r;
	}

	public String getSchemaVersion() {
		return schemaVersion;
	}

	public void setSchemaVersion(String schemaVersion) {
		this.schemaVersion = schemaVersion;
	}

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	@JsonIgnore
	public int getCode() {
		return errorCode;
	}

	public String getErrorName() {
		return errorName;
	}

	public void setErrorName(String errorName) {
		this.errorName = errorName;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	@JsonIgnore
	public String getMessage() {
		return summary;
	}

	public List<String> getWarnings() {
		return warnings;
	}

	public void setWarnings(List<String> warnings) {
		this.warnings = warnings != null ? new ArrayList<>(warnings) : new ArrayList<>();
	}

	public GmResult addWarning(String warning) {
		if (warning != null && !warning.isEmpty()) {
			this.warnings.add(warning);
		}
		return this;
	}

	public GmResult addWarnings(List<String> extraWarnings) {
		if (extraWarnings != null) {
			for (String w : extraWarnings) {
				addWarning(w);
			}
		}
		return this;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getTraceId() {
		return traceId;
	}

	public GmResult withTraceId(String traceId) {
		this.traceId = traceId;
		return this;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	public long getElapsedMs() {
		return elapsedMs;
	}

	public GmResult withElapsedMs(long elapsedMs) {
		this.elapsedMs = elapsedMs;
		return this;
	}

	public void setElapsedMs(long elapsedMs) {
		this.elapsedMs = elapsedMs;
	}

	public boolean isDryRun() {
		return dryRun;
	}

	public GmResult withDryRun(boolean dryRun) {
		this.dryRun = dryRun;
		return this;
	}

	public void setDryRun(boolean dryRun) {
		this.dryRun = dryRun;
	}

	public Object getDiff() {
		return diff;
	}

	public GmResult withDiff(Object diff) {
		this.diff = diff;
		return this;
	}

	public void setDiff(Object diff) {
		this.diff = diff;
	}

	/**
	 * 获取只读警告列表视图
	 *
	 * @return 不可变警告列表
	 */
	@JsonIgnore
	public List<String> unmodifiableWarnings() {
		return Collections.unmodifiableList(warnings);
	}
}