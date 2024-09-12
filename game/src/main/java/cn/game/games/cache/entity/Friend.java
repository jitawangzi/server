package cn.game.games.cache.entity;

import java.io.Serializable;

import cn.game.core.base.ServerContext;
import cn.game.games.cache.base.DbEntity;
import cn.game.protocol.protobuf.FriendMsg.FriendGiftInfo;

public class Friend implements Serializable, DbEntity {

	/**
	 * 玩家id
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 好友id
	 * @mbg.generated
	 */
	private Long friendId;
	/**
	 * 是否给这个好友送礼了
	 * @mbg.generated
	 */
	private Boolean gift;
	/**
	 * 好友是否给我送礼了
	 * @mbg.generated
	 */
	private Boolean gifted;
	/**
	 * 是否领取了该好友的奖励
	 * @mbg.generated
	 */
	private Boolean receive;
	/**
	 * 0好友，1黑名单
	 * @mbg.generated
	 */
	private Byte relation;
	/**
	 * 好友所在服务器id
	 * @mbg.generated
	 */
	private String serverId;
	/**
	 * 好友的亲密度
	 * @mbg.generated
	 */
	private Integer intimate;
	/**
	 * 亲密度等级
	 * @mbg.generated
	 */
	private Integer intimateLevel;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @mbg.generated
	 */
	public Long getPlayerId() {
		return playerId;
	}
	/**
	 * @mbg.generated
	 */
	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}
	/**
	 * @mbg.generated
	 */
	public Long getFriendId() {
		return friendId;
	}
	/**
	 * @mbg.generated
	 */
	public void setFriendId(Long friendId) {
		this.friendId = friendId;
	}
	/**
	 * @mbg.generated
	 */
	public Boolean getGift() {
		return gift;
	}
	/**
	 * @mbg.generated
	 */
	public void setGift(Boolean gift) {
		this.gift = gift;
	}
	/**
	 * @mbg.generated
	 */
	public Boolean getGifted() {
		return gifted;
	}
	/**
	 * @mbg.generated
	 */
	public void setGifted(Boolean gifted) {
		this.gifted = gifted;
	}
	/**
	 * @mbg.generated
	 */
	public Boolean getReceive() {
		return receive;
	}
	/**
	 * @mbg.generated
	 */
	public void setReceive(Boolean receive) {
		this.receive = receive;
	}
	/**
	 * @mbg.generated
	 */
	public Byte getRelation() {
		return relation;
	}
	/**
	 * @mbg.generated
	 */
	public void setRelation(Byte relation) {
		this.relation = relation;
	}
	/**
	 * @mbg.generated
	 */
	public String getServerId() {
		return serverId;
	}
	/**
	 * @mbg.generated
	 */
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
	/**
	 * @mbg.generated
	 */
	public Integer getIntimate() {
		return intimate;
	}
	/**
	 * @mbg.generated
	 */
	public void setIntimate(Integer intimate) {
		this.intimate = intimate;
	}
	/**
	 * @mbg.generated
	 */
	public Integer getIntimateLevel() {
		return intimateLevel;
	}
	/**
	 * @mbg.generated
	 */
	public void setIntimateLevel(Integer intimateLevel) {
		this.intimateLevel = intimateLevel;
	}
	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.FriendMapper.class;
	}
	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return new Object[] { playerId, friendId };
	}

	/** 好友 */
	public static final byte FRIEND = 0;
	/** 特别关注 */
	public static final byte ATTENTION = 1;
	/** 黑名单 */
	public static final byte BLACK = 2;

	public static Friend valueOf(long playerId, long friendId, byte relation) {
		return Friend.valueOf(playerId, friendId, ServerContext.getInstance().getServerId(), relation);
	}
	public static Friend valueOf(long playerId, long friendId, String serverId, byte relation) {
		Friend friend = new Friend();
		friend.setPlayerId(playerId);
		friend.setFriendId(friendId);
		friend.setGift(false);
		friend.setGifted(false);
		friend.setReceive(false);
		friend.setRelation(relation);
		friend.setIntimate(0);
		friend.setIntimateLevel(0);
		friend.setServerId(serverId);
		return friend;
	}

	public FriendGiftInfo toFriendGiftInfo() {
		FriendGiftInfo.Builder builder = FriendGiftInfo.newBuilder();
		builder.setGiftToFriend(this.getGift());
		builder.setGiftToMe(this.getGifted());
		builder.setReceive(this.getReceive());
		return builder.build();
	}
}