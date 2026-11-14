package cn.game.games.net.game.gm;

import java.util.List;

/**
 * GM指令Schema完整目录（按当前RunMode过滤安全等级后输出）
 *
 * @author shuyongqiang
 */
public class GmSchemaCatalog {

	private String schemaVersion = GmResult.SCHEMA_VERSION;
	private String runMode;
	private List<String> groups;
	private List<GmActionSchema> actions;

	public GmSchemaCatalog() {
	}

	public GmSchemaCatalog(String runMode, List<String> groups, List<GmActionSchema> actions) {
		this.runMode = runMode;
		this.groups = groups;
		this.actions = actions;
	}

	public String getSchemaVersion() {
		return schemaVersion;
	}

	public String getRunMode() {
		return runMode;
	}

	public List<String> getGroups() {
		return groups;
	}

	public List<GmActionSchema> getActions() {
		return actions;
	}
}