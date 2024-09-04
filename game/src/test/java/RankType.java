public enum RankType {
	LEVEL("level_rank"), ACTIVITY("activity_rank"), STAGE("stage_rank"), COMBAT_POWER("combat_power_rank");

	private final String key;

	RankType(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}