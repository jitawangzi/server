package cn.game.core.execute;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.util.DateUtil;

/**

一对一邮箱管理器，每个实体ID对应一个独立的邮箱

实现原有的每个ID一个邮箱的策略
*/
public class OneToOneMailboxManager implements MailboxManager {
	protected static final Logger LOGGER = LoggerFactory.getLogger(OneToOneMailboxManager.class);

	// 邮箱映射，每个id一个邮箱
	protected final ConcurrentMap<Long, ActorMailbox> mailboxes = new ConcurrentHashMap<>();
	protected final int maxQueueSize;
	/** 邮箱和清理共用的毫秒时钟，生产使用项目统一时间源。 */
	private final LongSupplier clock;

	/**
	 * 使用项目时间源创建一对一管理器。
	 * @param maxQueueSize 单个邮箱容量，非正数保持不限容量语义
	 */
	public OneToOneMailboxManager(int maxQueueSize) {
		this(maxQueueSize, DateUtil::currentTimeMillis);
	}

	/**
	 * 指定容量和时钟，供确定性清理测试使用。
	 * @param maxQueueSize 单个邮箱容量
	 * @param clock 返回毫秒时间的时钟
	 */
	OneToOneMailboxManager(int maxQueueSize, LongSupplier clock) {
		this.maxQueueSize = maxQueueSize;
		this.clock = clock;
	}

	/**
	
	将实体ID映射到邮箱ID，在一对一模式中，直接返回实体ID
	@param entityId 实体ID
	@return 邮箱ID
	*/
	protected long mapToMailboxId(long entityId) {
		if (entityId == 0) {
			return mapToMailboxId0();
		}
		return entityId;
	}
	protected long mapToMailboxId0() {
			// 0的时候，不对应任何实体，随机返回一个邮箱ID，避免0的任务堆积到一起。并且避开默认队列
		return ThreadLocalRandom.current().nextLong(Long.MAX_VALUE - 128, Long.MAX_VALUE);
	}

	@Override
	public ActorMailbox getOrCreateMailbox(long entityId) {
		long mailboxId = mapToMailboxId(entityId);
		return mailboxes.computeIfAbsent(mailboxId, id -> new ActorMailbox(id, maxQueueSize, clock));
	}

	@Override
	public ActorMailbox enqueueTask(long entityId, TaskWrapper<?> task, boolean fast) {
		long mailboxId = mapToMailboxId(entityId);
		AtomicReference<ActorMailbox> accepted = new AtomicReference<>();
		// 与退役使用同一个实际键，避免获取旧实例后才入队形成孤立消费者。
		mailboxes.compute(mailboxId, (id, current) -> {
			ActorMailbox mailbox = current == null ? new ActorMailbox(id, maxQueueSize, clock) : current;
			if (mailbox.offerTask(task, fast)) { accepted.set(mailbox); }
			return mailbox;
		});
		return accepted.get();
	}

	@Override
	public ActorMailbox getMailbox(long entityId) {
		return mailboxes.get(mapToMailboxId(entityId));
	}

	@Override
	public boolean removeMailbox(long entityId) {
		long mailboxId = mapToMailboxId(entityId);
		ActorMailbox mailbox = mailboxes.get(mailboxId);
		return mailbox != null && removeMailbox(mailbox, -1);
	}

	@Override
	public boolean removeMailbox(ActorMailbox mailbox, long idleTimeoutMs) {
		AtomicBoolean removed = new AtomicBoolean();
		mailboxes.computeIfPresent(mailbox.getEntityId(), (id, current) -> {
			if (current == mailbox && current.retireIfIdle(clock.getAsLong(), idleTimeoutMs)) {
				removed.set(true);
				return null;
			}
			return current;
		});
		return removed.get();
	}

	@Override
	public ConcurrentMap<Long, ActorMailbox> getMailboxes() {
		return mailboxes;
	}

	@Override
	public int cleanIdleMailboxes(long idleTimeoutMs) {
		int removed = 0;
		for (ActorMailbox mailbox : mailboxes.values()) {
			if (removeMailbox(mailbox, idleTimeoutMs)) { removed++; }
		}
		if (removed > 0) {
			LOGGER.info("Mailbox cleaner: removed {} idle mailboxes", removed);
		}
		return removed;
	}

	@Override
	public void close() {
		mailboxes.clear();
	}
}
