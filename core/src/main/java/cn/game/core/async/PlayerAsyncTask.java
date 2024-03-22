package cn.game.core.async;


public abstract class PlayerAsyncTask implements AsyncTask {

	private long playerId;
	
	public PlayerAsyncTask(long playerId) {
		this.playerId = playerId;
	}
	
	@Override
	public int doStart() {
		return STAGE_START_DONE;
	}

	@Override
	public int doStop() {
		return STAGE_STOP_DONE;
	}

}
