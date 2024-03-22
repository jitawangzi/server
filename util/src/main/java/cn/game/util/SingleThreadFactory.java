package cn.game.util;

import java.util.concurrent.ThreadFactory;

public class SingleThreadFactory implements ThreadFactory {
	private int _prio;
	private String _name;
	private ThreadGroup _group;

	public SingleThreadFactory(String name, int prio) {
		_prio = prio;
		_name = name;
		_group = new ThreadGroup(_name);
	}

	public Thread newThread(Runnable r) {
		Thread t = new Thread(_group, r);
		t.setName(_name);
		t.setPriority(_prio);
		return t;
	}

	public ThreadGroup getGroup() {
		return _group;
	}
}
