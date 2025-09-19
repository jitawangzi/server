package cn.game.core.base;

public enum RunMode {

	DEV, TEST, CHECK, PRESSURE, PRODUCTION;

	public boolean isDev() {
		return this.equals(DEV);
	}
	public boolean isTest() {
		return this.equals(TEST);
	}
	public boolean isCheck() {
		return this.equals(CHECK);
	}

	public boolean isPressure() {
		return this.equals(PRESSURE);
	}
	public boolean isProduction() {
		return this.equals(PRODUCTION);
	}

}
