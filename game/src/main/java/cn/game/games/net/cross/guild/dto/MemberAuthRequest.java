package cn.game.games.net.cross.guild.dto;

import java.util.List;

/**
 * 成员权限请求
 */
public class MemberAuthRequest {
    private long operatorId;
    private String operatorName;
    private int optType; // 1:同意加入 2:拒绝加入 3:踢人
    private List<Long> targetPlayerIds;

    // Getters and setters
    public long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(long operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public int getOptType() {
        return optType;
    }

    public void setOptType(int optType) {
        this.optType = optType;
    }

    public List<Long> getTargetPlayerIds() {
        return targetPlayerIds;
    }

    public void setTargetPlayerIds(List<Long> targetPlayerIds) {
        this.targetPlayerIds = targetPlayerIds;
    }
}

