package cn.game.core.async;


public abstract class BaseAsyncTask implements AsyncTask {

	@Override
	public int doStart() {
		return STAGE_START_DONE;
	}

	@Override
	public int doStop() {
		return STAGE_STOP_DONE;
	}

}
