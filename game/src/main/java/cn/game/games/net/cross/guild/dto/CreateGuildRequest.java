package cn.game.games.net.cross.guild.dto;

/**
 * 创建公会请求
 */
public class CreateGuildRequest {
    private String name;
    private long createPlayerId;
    private String createPlayerName;
    private int power;
    private String serverId;
    private int icon;
    private String notice;
    private String declaration;

    public CreateGuildRequest(String name, long createPlayerId, String createPlayerName, int power, String serverId) {
        this.name = name;
        this.createPlayerId = createPlayerId;
        this.createPlayerName = createPlayerName;
        this.power = power;
        this.serverId = serverId;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreatePlayerId() {
        return createPlayerId;
    }

    public void setCreatePlayerId(long createPlayerId) {
        this.createPlayerId = createPlayerId;
    }

    public String getCreatePlayerName() {
        return createPlayerName;
    }

    public void setCreatePlayerName(String createPlayerName) {
        this.createPlayerName = createPlayerName;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
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
}

