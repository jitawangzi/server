package cn.game.games.core.push;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.Message;

import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.PlayerMsg.BatchItem;
import cn.game.protocol.protobuf.PlayerMsg.PlayerBatchPush_01100100;
import cn.game.util.Config;

public class MessageQueue {
	private Map<List<String>, Queue<PushMessage>> delayedBuffer;
	private BlockingQueue<PushMessage> immediateQueue;
	private TagSystem tagSystem;
	private ScheduledExecutorService scheduler;
	private ExecutorService immediateExecutor;
	private final int BUFFER_TIME_MS = 1000;
//	private final int MESSAGE_BATCH_COMBINE_SIZE = 100000;
//	private final int BATCH_SIZE = 10;
	private BiConsumer<Long, Message> sendToPlayer;

	public MessageQueue(TagSystem tagSystem, BiConsumer<Long, Message> sendToPlayer) {
		this.delayedBuffer = new ConcurrentHashMap<>();
		this.immediateQueue = new LinkedBlockingQueue<>();
		this.tagSystem = tagSystem;
		this.scheduler = Executors.newScheduledThreadPool(1, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "DelayedMessagesProcess");
			}
		});
		this.immediateExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "ImmediateMessagesProcess");
			}
		});
		this.scheduler.scheduleAtFixedRate(this::processDelayedMessages, BUFFER_TIME_MS, BUFFER_TIME_MS, TimeUnit.MILLISECONDS);

		this.immediateExecutor.submit(this::processImmediateMessages);
		this.sendToPlayer = sendToPlayer;
	}

	public void addMessage(Message protoMessage, boolean immediate, String... tags) {
		List<String> tagList = tags == null || tags.length == 1 && StringUtils.isEmpty(tags[0]) ? Collections.emptyList()
				: Arrays.asList(tags);
		PushMessage msg = new PushMessage(protoMessage, immediate, tagList);
		if (immediate) {
			immediateQueue.offer(msg);
		} else {
			delayedBuffer.computeIfAbsent(Arrays.asList(tags), k -> new ConcurrentLinkedQueue<>()).offer(msg);
		}
	}

	private void processDelayedMessages() {
		for (Map.Entry<List<String>, Queue<PushMessage>> entry : delayedBuffer.entrySet()) {
			List<String> tags = entry.getKey();
			Queue<PushMessage> messages = entry.getValue();
			if (messages.size() < Config.PUSH_MESSAGE_BATCH_COMBINE_SIZE) {
				PushMessage pushMessage = null;
				while ((pushMessage = messages.poll()) != null) {
					broadcastMessage(tags, pushMessage.protoMessage);
				}
			} else {
				// 消息过多合并后在广播。
				List<Message> batch = new ArrayList<>();
				for (int i = 0; i < Config.PUSH_MESSAGE_BATCH_SIZE; i++) {
					batch.add(messages.poll().protoMessage);
				}
				if (!batch.isEmpty()) {
					Message combinedMessage = combineMessages(batch);
					broadcastMessage(tags, combinedMessage);
				}
			}
		}
	}

	private void processImmediateMessages() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				PushMessage message = immediateQueue.take();
				broadcastMessage(message.tags, message.protoMessage);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private PlayerBatchPush_01100100 combineMessages(List<Message> messages) {
		PlayerBatchPush_01100100.Builder batchBuilder = PlayerBatchPush_01100100.newBuilder();

		for (Message message : messages) {
			int msgId = PbProtocol.getInstance().getMsgId(message.getClass().getSimpleName());
			BatchItem.Builder itemBuilder = BatchItem.newBuilder().setMsgId(msgId)
					.setPayload(message.toByteString());

			batchBuilder.addItems(itemBuilder);
		}
		return batchBuilder.build();
	}

	private void broadcastMessage(List<String> tags, Message message) {
		Set<Long> players = tags.isEmpty() ? tagSystem.getAllPlayers() : tagSystem.getPlayersByTags(tags);
		for (Long playerId : players) {
			sendToPlayer.accept(playerId, message);
		}
	}

	public void stop() {
		scheduler.shutdown();
		immediateExecutor.shutdownNow();
	}
}