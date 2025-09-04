package cn.game.games.net.game.module.shop.limitedtimegift;

import cn.game.protocol.protobuf.ShopMsg.LimitedTimeGiftInfo;
import cn.game.util.DateUtil;

public class LimitedTimeGiftData {
	
	private int id ; 
	private int buyCount; 
	private long expireTime; //过期时间戳
	
	public LimitedTimeGiftInfo toProto() {
		LimitedTimeGiftInfo.Builder builder = LimitedTimeGiftInfo.newBuilder();
		builder.setId(id);
		builder.setBuyCount(buyCount);
		builder.setEndTime((int) (expireTime/1000)); 
		return builder.build();
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getBuyCount() {
		return buyCount;
	}
	public void setBuyCount(int buyCount) {
		this.buyCount = buyCount;
	}
	public long getExpireTime() {
		return expireTime;
	}
	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}

	
}
