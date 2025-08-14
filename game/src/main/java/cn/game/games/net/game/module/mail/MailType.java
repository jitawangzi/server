package cn.game.games.net.game.module.mail;

public enum MailType {
    Person(0),
    Gloabl(1) ,
    System(2),
    DASHENG_XUN_SHAN(3);

   private int value;
    private MailType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
    public static MailType valueOf(int value) {
        for (MailType type : MailType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }
}

