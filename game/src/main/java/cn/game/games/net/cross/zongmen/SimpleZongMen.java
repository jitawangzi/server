package cn.game.games.net.cross.zongmen;

import cn.game.protocol.protobuf.ZongMenMsg;

/**
 * @ClassName SimpleZongMen
 *
 * @description: 存储到redis 的宗门简介信息
 * @author: ly
 * @create: 2025-02-05 16:22 @Version 1.0
 */
public class SimpleZongMen {
    /**宗门id*/
    long id;
    /**宗门名称*/
    String name;
    /**宗门总战力*/
    int totalPower;
    /**宗门图标*/
    int icon;
    /**等级*/
    int lv;
    /**总人数*/
    int num;
    /**是否允许自动加入 true 自动加入 ,false 不自动加入 */
    boolean isAutoJoin;
    /** 天道的等级 */
    int tianDaoLevel;

    public ZongMenMsg.ZongMenSimpleInfoProto toProto() {
        ZongMenMsg.ZongMenSimpleInfoProto.Builder builder = ZongMenMsg.ZongMenSimpleInfoProto.newBuilder();
        builder.setId(id);
        builder.setName(name);
        builder.setTotalPower(totalPower);
        builder.setIcon(icon);
        builder.setLevel(lv);
        builder.setIsAutoJoin(isAutoJoin);
        builder.setTianDaoLevel(tianDaoLevel);
        builder.setMemberNum(num);
        return builder.build();
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotalPower() {
        return totalPower;
    }

    public void setTotalPower(int totalPower) {
        this.totalPower = totalPower;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public boolean isAutoJoin() {
        return isAutoJoin;
    }

    public void setAutoJoin(boolean autoJoin) {
        isAutoJoin = autoJoin;
    }

    public int getTianDaoLevel() {
        return tianDaoLevel;
    }

    public void setTianDaoLevel(int tianDaoLevel) {
        this.tianDaoLevel = tianDaoLevel;
    }
}
