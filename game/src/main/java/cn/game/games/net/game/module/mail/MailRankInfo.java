package cn.game.games.net.game.module.mail;

public class MailRankInfo {

    private String name;
    private int rankId;
    private int figure;
    private String playerId;

    public MailRankInfo(String name, int rankId, int figure, String playerId) {
        this.name = name;
        this.rankId = rankId;
        this.figure = figure;
        this.playerId = playerId;
    }

    public MailRankInfo() {
    }


    public String getName() {
        return name;
    }

    public int getRankId() {
        return rankId;
    }

    public int getFigure() {
        return figure;
    }

    public String getPlayerId() {
        return playerId;
    }
}
