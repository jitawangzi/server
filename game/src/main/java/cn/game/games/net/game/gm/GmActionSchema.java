package cn.game.games.net.game.gm;

import java.util.List;

/**
 * GM动作Schema元信息模型
 *
 * @author shuyongqiang
 */
public class GmActionSchema {

	private String id;
	private String group;
	private String action;
	private List<String> aliases;
	private String description;
	private GmSecurityTier security;
	private GmScope scope;
	private boolean dryRunSupported;
	private String sideEffects;
	private boolean idempotent;
	private boolean readOnly;
	private String example;
	private List<GmParamSchema> params;

	public GmActionSchema() {
	}

	public GmActionSchema(String id, String group, String action, List<String> aliases, String description,
			GmSecurityTier security, GmScope scope, boolean dryRunSupported, List<GmParamSchema> params) {
		this(id, group, action, aliases, description, security, scope, dryRunSupported, "", false, false, "", params);
	}

	public GmActionSchema(String id, String group, String action, List<String> aliases, String description,
			GmSecurityTier security, GmScope scope, boolean dryRunSupported, String sideEffects,
			boolean idempotent, String example, List<GmParamSchema> params) {
		this(id, group, action, aliases, description, security, scope, dryRunSupported, sideEffects, idempotent, false, example, params);
	}

	public GmActionSchema(String id, String group, String action, List<String> aliases, String description,
			GmSecurityTier security, GmScope scope, boolean dryRunSupported, String sideEffects,
			boolean idempotent, boolean readOnly, String example, List<GmParamSchema> params) {
		this.id = id;
		this.group = group;
		this.action = action;
		this.aliases = aliases;
		this.description = description;
		this.security = security;
		this.scope = scope;
		this.dryRunSupported = dryRunSupported;
		this.sideEffects = sideEffects;
		this.idempotent = idempotent;
		this.readOnly = readOnly;
		this.example = example;
		this.params = params;
	}

	public String getId() {
		return id;
	}

	public String getGroup() {
		return group;
	}

	public String getAction() {
		return action;
	}

	public List<String> getAliases() {
		return aliases;
	}

	public String getDescription() {
		return description;
	}

	public GmSecurityTier getSecurity() {
		return security;
	}

	public GmSecurityTier getTier() {
		return security;
	}

	public GmScope getScope() {
		return scope;
	}

	public boolean isDryRunSupported() {
		return dryRunSupported;
	}

	public String getSideEffects() {
		return sideEffects;
	}

	public boolean isIdempotent() {
		return idempotent;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public String getExample() {
		return example;
	}

	public List<GmParamSchema> getParams() {
		return params;
	}
}