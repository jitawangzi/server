package cn.game.util.file;

import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.util.MailUtil;

/**
 * 只监听xml文件修改
 * 2018年11月10日 下午1:54:30
 * @author SYQ
 */
public class WatchServiceManager implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(WatchServiceManager.class);
	private Map<String, ResourceListener> resources = new HashMap<>();
	/** 实际资源加载前执行，身份撤销不能放在加载成功后的消费者中。 */
	private final java.util.concurrent.atomic.AtomicLong resourceVersion = new java.util.concurrent.atomic.AtomicLong();

	/** @return 实际分派前递增的资源版本；恢复原文件不能倒退。 */
	public long resourceVersion() { return resourceVersion.get(); }

	/**
	 * 分派实际文件事件；观察者先于任何加载执行，加载异常也不能恢复旧身份。
	 * @param kind 文件系统事件类型
	 * @param path 相对文件名；OVERFLOW 可为空
	 */
	public void dispatchChange(WatchEvent.Kind<?> kind, Path path) {
		if (kind == StandardWatchEventKinds.OVERFLOW) {
			resourceVersion.incrementAndGet();
			return;
		}
		if (kind != StandardWatchEventKinds.ENTRY_CREATE && kind != StandardWatchEventKinds.ENTRY_MODIFY) { return; }
		String fileName = path.getFileName().toString();
		if (fileName.startsWith(".") || !fileName.endsWith(".xml")) { return; }
		resourceVersion.incrementAndGet();
		ResourceListener listener = resources.get(fileName.substring(0, fileName.length() - 4));
		if (listener == null) { log.warn("配置文件[{}]没有对应的解析器", fileName); return; }
		if (kind == StandardWatchEventKinds.ENTRY_MODIFY) { listener.reload(); } else { listener.load(); }
		log.info("RUNTIME_RESOURCE_LOADED name={} version={}", fileName, resourceVersion.get());
	}

	private String[] watchDirs;

	private static WatchServiceManager instance = new WatchServiceManager();

	public static WatchServiceManager getInstance() {
		return instance;
	}

	private WatchServiceManager() {
	};

	public WatchServiceManager setWatchDirs(String... watchDirs) {
		this.watchDirs = watchDirs;
		return this;
	}

	public void register(ResourceListener listener) {
		this.resources.put(listener.name(), listener);
	}
	public void registerAndLoad(ResourceListener listener) {
		register(listener);
		listener.load();
	}

	public static void main(String[] a) {
		WatchServiceManager.getInstance().run();
	}

	@Override
	public void run() {

		List<Path> list = new ArrayList<>();
		try {
			if (watchDirs == null) {
				watchDirs = new String[] { "." };
			}
			for (String dir : watchDirs) {
				list.add(Paths.get(ClassLoader.getSystemResource(dir).toURI()));
			}
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		for (Path path2 : list) {
			log.info("监听文件路径： " + path2);
		}
		try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
			// 给path路径加上文件观察服务

			for (Path path : list) {
				path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
			}
//			path2.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
			// start an infinite loop
			while (true) {
				final WatchKey key = watchService.take();
				List<WatchEvent<?>> pollEvents = key.pollEvents();
				for (WatchEvent<?> watchEvent : pollEvents) {

					final WatchEvent.Kind<?> kind = watchEvent.kind();

					try {
						dispatchChange(kind, kind == StandardWatchEventKinds.OVERFLOW ? null : (Path) watchEvent.context());
					} catch (Exception e) {
						log.error("配置文件热更新异常：", e);
						MailUtil.reportException("配置文件热更新异常", ExceptionUtils.getFullStackTrace(e));
					}
					// 删除事件
					// if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
					//
					// }
				}
				// reset the keyf
				boolean valid = key.reset();
				// exit loop if the key is not valid (if the directory was
				// deleted,for
				if (!valid) {
					break;
				}
			}

		} catch (Throwable e) {
			log.error("配置文件热更新失败：", e);
		}

	}

}
