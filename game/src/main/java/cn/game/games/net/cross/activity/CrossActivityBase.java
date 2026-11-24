package cn.game.games.net.cross.activity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.core.event.server.ServerEvent;
import cn.game.games.net.cross.activity.dto.CrossActivityActionResult;
import cn.game.games.net.game.module.activity.ActivityBase;

/**
 * 跨服活动领域模型基类
 * 
 * @author shuyongqiang
 */
public class CrossActivityBase extends ActivityBase {

	/** 作用域键（对应 t_game_activity.server_id，如 "all" 或具体的 serverId） */
	protected String scopeKey;

	/** 瞬态运行时脏标记 */
	@JsonIgnore
	private transient volatile boolean dirty;

	/** 瞬态运行时上次保存时间戳 */
	@JsonIgnore
	private transient volatile long lastSaveTime;

	public CrossActivityBase() {
	}

	public String getScopeKey() {
		return scopeKey;
	}

	public void setScopeKey(String scopeKey) {
		this.scopeKey = scopeKey;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void markDirty() {
		this.dirty = true;
	}

	public void clearDirty() {
		this.dirty = false;
	}

	public long getLastSaveTime() {
		return lastSaveTime;
	}

	public void setLastSaveTime(long lastSaveTime) {
		this.lastSaveTime = lastSaveTime;
	}

	/**
	 * 从数据库加载后的钩子，供子类重建瞬态索引等
	 */
	public void afterLoadFromDb() {
	}

	/**
	 * 严禁直接向 ServerEventBus 注册，外部事件一律由 CrossActivityService 统一投递 mailbox
	 */
	@Override
	public void registerEvent() {
		// no-op: 由 CrossActivityService 统一投递 mailbox 驱动
	}

	@Override
	public void unregisterEvent() {
		// no-op: 由 CrossActivityService 统一投递 mailbox 驱动
	}

	@Override
	protected void afterStart() {
		super.afterStart();
		onStart();
		markDirty();
	}

	@Override
	protected void afterEnd() {
		super.afterEnd();
		onEnd();
		markDirty();
	}

	@Override
	protected void afterDestroy() {
		super.afterDestroy();
		onDestroy();
		markDirty();
	}

	@Override
	public boolean newDay() {
		boolean res = super.newDay();
		onNewDay();
		markDirty();
		return true;
	}

	/** 子类生命周期钩子 */
	protected void onStart() {
	}

	protected void onEnd() {
	}

	protected void onDestroy() {
	}

	protected void onNewDay() {
	}

	/**
	 * 处理外部事件（在 activityUid 邮箱内执行）
	 * 
	 * @param event 服务端事件
	 */
	public void handleEvent(ServerEvent event) {
	}

	/**
	 * 执行玩家业务动作（在 activityUid 邮箱内执行）
	 * 供业务子类覆写以处理具体的跨服活动玩法协议
	 * 
	 * @param playerId 玩家ID
	 * @param actionType 动作类型
	 * @param payloadJson 动作载荷JSON
	 * @return 动作执行结果
	 */
	public CrossActivityActionResult executePlayerAction(long playerId, String actionType, String payloadJson) {
		markDirty();
		return CrossActivityActionResult.success(payloadJson);
	}

	/**
	 * 玩家参与跨服活动校验与处理（在 activityUid 邮箱内执行）
	 * 
	 * @param playerId 玩家ID
	 * @param serverId 玩家所在区服ID
	 * @param extraData 附加数据
	 * @return 是否允许参与/初始化成功
	 */
	public boolean join(long playerId, String serverId, java.util.Map<String, Object> extraData) {
		markDirty();
		return true;
	}
}
