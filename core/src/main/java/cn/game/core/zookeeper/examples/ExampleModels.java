package cn.game.core.zookeeper.examples;

/**
 * 示例：普通业务模型
 */
public class ExampleModels {

    public static class VirtualServerView {
        public String ID;
        public String name;
        public Integer playerMaxCount;
        public Integer seq;
        public String openTime;

        public VirtualServerView() {}
        public VirtualServerView(String ID, String name, Integer playerMaxCount, Integer seq, String openTime) {
            this.ID = ID;
            this.name = name;
            this.playerMaxCount = playerMaxCount;
            this.seq = seq;
            this.openTime = openTime;
        }
    }

    public static class PlayerInfo {
        public long id;
        public String nickname;
        public int level;

        public PlayerInfo() {}
        public PlayerInfo(long id, String nickname, int level) {
            this.id = id;
            this.nickname = nickname;
            this.level = level;
        }
    }
}

