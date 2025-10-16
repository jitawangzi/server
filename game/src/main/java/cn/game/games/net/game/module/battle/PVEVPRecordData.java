package cn.game.games.net.game.module.battle;

public class PVEVPRecordData {

    public int result;
    public String name;
    public int level;
    public int combatEffectiveness;
    public int scoreChange;
    public int head;
    public int headFrame;
    public long battleTime;
    public int type;
    public long playerId;

    public PVEVPRecordData(int result, String name, int level, int combatEffectiveness, int scoreChange, int head, int headFrame, long battleTime, int type, long playerId) {
        this.result = result;
        this.name = name;
        this.level = level;
        this.combatEffectiveness = combatEffectiveness;
        this.scoreChange = scoreChange;
        this.head = head;
        this.headFrame = headFrame;
        this.battleTime = battleTime;
        this.type = type;
        this.playerId = playerId;
    }
    public PVEVPRecordData() {
    }

    @Override
    public String toString() {
        return  "result:" + result +
                ", name:" + name +
                ", level:" + level +
                ", combatEffectiveness:" + combatEffectiveness +
                ", scoreChange:" + scoreChange +
                ", head:" + head +
                ", headFrame:" + headFrame +
                ", battleTime:" + battleTime +
                ", type:" + type +
                ", playerId:" + playerId;
    }
}
