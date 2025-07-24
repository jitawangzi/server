package cn.game.games.net.cross.zongmen;

import cn.game.protocol.protobuf.ZongMenMsg;

import java.util.ArrayList;
import java.util.List;

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
    /**1 快速加入、2 需要验证加入、3 不可加入； */
    int isAutoJoin;
    /**申请的玩家id集合*/
    List<Long> applyPidList = new ArrayList<>();


    public ZongMenMsg.ZongMenSimpleInfoProto toProto() {
        ZongMenMsg.ZongMenSimpleInfoProto.Builder builder = ZongMenMsg.ZongMenSimpleInfoProto.newBuilder();
        builder.setId((int) id);
        builder.setName(name);
        builder.setTotalPower(totalPower);
        builder.setIcon(icon);
        builder.setLevel(lv);
        builder.setIsAutoJoin(isAutoJoin);
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

    public int getIsAutoJoin() {
        return isAutoJoin;
    }

    public void setIsAutoJoin(int isAutoJoin) {
        this.isAutoJoin = isAutoJoin;
    }

    public List<Long> getApplyPidList() {
        return applyPidList;
    }

    public void setApplyPidList(List<Long> applyPidList) {
        this.applyPidList = applyPidList;
    }
}
