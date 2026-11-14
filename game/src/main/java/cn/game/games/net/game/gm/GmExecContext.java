package cn.game.games.net.game.gm;

import java.util.ArrayList;
import java.util.List;

import cn.game.core.base.RunMode;
import cn.game.games.cache.entity.Player;

/**
 * 单次GM动作执行上下文，可作为可选参数注入动作方法
 *
 * @author shuyongqiang
 */
public class GmExecContext {

	private final Player player;
	private final long playerId;
	private final String command;
	private final String traceId;
	private final boolean dryRun;
	private final RunMode runMode;
	private final String source;
	private final List<String> warnings = new ArrayList<>();

	public GmExecContext(Player player, long playerId, String command, String traceId, boolean dryRun, RunMode runMode, String source) {
		this.player = player;
		this.playerId = playerId;
		this.command = command;
		this.traceId = traceId;
		this.dryRun = dryRun;
		this.runMode = runMode;
		this.source = source;
	}

	public Player getPlayer() {
		return player;
	}

	public long getPlayerId() {
		return playerId;
	}

	public String getCommand() {
		return command;
	}

	public String getTraceId() {
		return traceId;
	}

	public boolean isDryRun() {
		return dryRun;
	}

	public RunMode getRunMode() {
		return runMode;
	}

	public String getSource() {
		return source;
	}

	public void addWarning(String warning) {
		if (warning != null && !warning.isEmpty()) {
			this.warnings.add(warning);
		}
	}

	public List<String> getWarnings() {
		return warnings;
	}
}