package cn.game.core.base;

public enum RunMode {

	TEST, CHECK, PRO;

	public boolean isTest() {
		return this.equals(TEST);
	}
	public boolean isCheck() {
		return this.equals(CHECK);
	}

}
