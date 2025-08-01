package cn.game.core.execute;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**

一对一邮箱管理器，每个实体ID对应一个独立的邮箱

实现原有的每个ID一个邮箱的策略
*/
public class OneToOneMailboxManager implements MailboxManager {
	protected static final Logger LOGGER = LoggerFactory.getLogger(OneToOneMailboxManager.class);

	// 邮箱映射，每个id一个邮箱
	protected final ConcurrentMap<Long, ActorMailbox> mailboxes = new ConcurrentHashMap<>();
	protected final int maxQueueSize;

	public OneToOneMailboxManager(int maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
	}

	/**
	
	将实体ID映射到邮箱ID，在一对一模式中，直接返回实体ID
	@param entityId 实体ID
	@return 邮箱ID
	*/
	protected long mapToMailboxId(long entityId) {
//		if (entityId == 0) {
//			throw new IllegalArgumentException("Entity ID cannot be 0, it should not have a mailbox.");
//		}
		if (entityId == 0) {
			// 0的时候，不对应任何实体，随机返回一个邮箱ID，避免0的任务堆积到一起。并且避开默认队列
			return ThreadLocalRandom.current().nextInt(1000000000, 1000000128);
		}
		return entityId;
	}

	@Override
	public ActorMailbox getOrCreateMailbox(long entityId) {
		long mailboxId = mapToMailboxId(entityId);
		return mailboxes.computeIfAbsent(mailboxId, id -> new ActorMailbox(id, maxQueueSize));
	}

	@Override
	public ActorMailbox getMailbox(long entityId) {
		return mailboxes.get(mapToMailboxId(entityId));
	}

	@Override
	public boolean removeMailbox(long entityId) {
		long mailboxId = mapToMailboxId(entityId);
		ActorMailbox mailbox = mailboxes.get(mailboxId);
		if (mailbox != null && mailbox.isEmpty() && !mailbox.isProcessing()) {
			return mailboxes.remove(mailboxId) != null;
		}
		return false;
	}

	@Override
	public ConcurrentMap<Long, ActorMailbox> getMailboxes() {
		return mailboxes;
	}

	@Override
	public int cleanIdleMailboxes(long idleTimeoutMs) {
		long now = System.currentTimeMillis();
		int removed = 0;
		Iterator<Map.Entry<Long, ActorMailbox>> it = mailboxes.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Long, ActorMailbox> entry = it.next();
			ActorMailbox mailbox = entry.getValue();
			if (mailbox.isEmpty() && !mailbox.isProcessing() && now - mailbox.getLastAccessTime() > idleTimeoutMs) {
				it.remove();
				removed++;
			}
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
