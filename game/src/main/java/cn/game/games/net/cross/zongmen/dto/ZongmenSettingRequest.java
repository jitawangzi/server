package cn.game.games.net.cross.zongmen.dto;

/**
 * 宗门设置请求
 */
public class ZongmenSettingRequest {
    private long operatorId;
    private String operatorName;
    private String name;
    private String wx;
    private String notice;
    private String declaration;
    private int icon;
    private int autoJoin;
    private int tianDaoLevel;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWx() {
        return wx;
    }

    public void setWx(String wx) {
        this.wx = wx;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public String getDeclaration() {
        return declaration;
    }

    public void setDeclaration(String declaration) {
        this.declaration = declaration;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getAutoJoin() {
        return autoJoin;
    }

    public void setAutoJoin(int autoJoin) {
        this.autoJoin = autoJoin;
    }

    public int getTianDaoLevel() {
        return tianDaoLevel;
    }

    public void setTianDaoLevel(int tianDaoLevel) {
        this.tianDaoLevel = tianDaoLevel;
    }
}

