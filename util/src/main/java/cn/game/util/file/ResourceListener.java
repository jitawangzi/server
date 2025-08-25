package cn.game.util.file;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ResourceListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceListener.class);
	private final java.util.concurrent.atomic.AtomicLong version = new java.util.concurrent.atomic.AtomicLong(0);
	private List<Runnable> reloadConsumers = new CopyOnWriteArrayList<>();

	public abstract void load();

	public abstract String name();
	
	public ResourceListener addReloadConsumer(Runnable runnable) {
	    if (runnable != null) {
	        this.reloadConsumers.add(runnable);
	    }
	    return this;
	}
	
	public void reload() {
		load(); 
		long ver = version.incrementAndGet();
		for (Runnable runnable : reloadConsumers) {
	        try {
	            runnable.run();
	        } catch (Throwable t) {
	        	LOGGER.warn("Reload consumer error on {}, ver={}", name(), ver, t);
	        }
		}
	}
	public long currentVersion() {
	    return version.get();
	}
}
