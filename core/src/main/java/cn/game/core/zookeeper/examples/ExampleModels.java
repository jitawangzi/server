package cn.game.core.zookeeper.examples;

/**
 * 示例：定义两个配置类型，键类型分别为 String 与 long。
 */
public class ExampleModels {

    // String 键
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

    // long 键
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

