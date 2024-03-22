package cn.game.games.cache.entity;

import java.io.Serializable;

public class Lead  implements Serializable{

	/**  */
	private static final long	serialVersionUID	= 8799172789554063746L;

	/**  */
    private Long playerId;

    /** 举贤id:1, 2*/
    private Byte leadId;

    /** 免费次数 */
    private Integer freeCount;

    /**  */
    private String lastFreeLeadDate;

    /** 累计消耗的资源 */
    private Integer consume;

    /** 当前奖励组的索引 */
    private Short lootSlot;
    
    private Boolean isFirst ; 

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public Byte getLeadId() {
        return leadId;
    }

    public void setLeadId(Byte leadId) {
        this.leadId = leadId;
    }

    public Integer getFreeCount() {
        return freeCount;
    }

    public void setFreeCount(Integer freeCount) {
        this.freeCount = freeCount;
    }

    public String getLastFreeLeadDate() {
        return lastFreeLeadDate;
    }

    public void setLastFreeLeadDate(String lastFreeLeadDate) {
        this.lastFreeLeadDate = lastFreeLeadDate;
    }

    public Integer getConsume() {
        return consume;
    }

    public void setConsume(Integer consume) {
        this.consume = consume;
    }

    public Short getLootSlot() {
        return lootSlot;
    }

    public void setLootSlot(Short lootSlot) {
        this.lootSlot = lootSlot;
    }

	
	public Boolean getIsFirst() {
	
		return isFirst;
	}

	
	public void setIsFirst(Boolean isFirst) {
	
		this.isFirst = isFirst;
	}
    
}