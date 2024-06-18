package cn.game.core.base;

public enum RunMode {

	TEST, CHECK, PRESSURE, Production;

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
		return this.equals(Production);
	}

}
