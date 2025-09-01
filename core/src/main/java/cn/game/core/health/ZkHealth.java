package cn.game.core.health;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;

import cn.game.util.ZkHelper;

public class ZkHealth {

	private final AtomicReference<ConnectionState> lastState = new AtomicReference<>(ConnectionState.LOST);
	private final AtomicBoolean connected = new AtomicBoolean(false);
	private int consecutiveFail = 0;
	private final CuratorFramework curator ; 

	public ZkHealth(CuratorFramework curator) {
		this.curator = curator; 
	}
	void attach() {
		curator.getConnectionStateListenable().addListener((c, state) -> {
			lastState.set(state);
			switch (state) {
			case CONNECTED:
			case RECONNECTED:
				connected.set(true);
				break;
			default:
				connected.set(false);
			}
		});
		boolean init = ZkHelper.curator.getZookeeperClient().isConnected();
		connected.set(init);
		lastState.set(init ? ConnectionState.CONNECTED : ConnectionState.SUSPENDED);
	}

	boolean probeOnce() {
		boolean ok = connected.get();
		if (ok)
			consecutiveFail = 0;
		else
			consecutiveFail++;
		return ok;
	}

	boolean breached(int threshold) {
		return consecutiveFail >= threshold;
	}

	String stateName() {
		ConnectionState s = lastState.get();
		return s == null ? "UNKNOWN" : s.name();
	}

	int consecutiveFail() {
		return consecutiveFail;
	}

}
