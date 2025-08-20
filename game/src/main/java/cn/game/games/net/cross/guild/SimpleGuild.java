package cn.game.games.net.cross.guild;

import cn.game.protocol.protobuf.GuildMsg;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName SimpleGuild
 *
 * @description: 存储到redis 的公会简介信息
 * @author: ly
 * @create: 2025-02-05 16:22 @Version 1.0
 */
public class SimpleGuild {
    /**公会id*/
    long id;
    /**公会名称*/
    String name;
    /**公会总战力*/
    int totalPower;
    /**公会图标*/
    int icon;
    /**等级*/
    int lv;
    /**总人数*/
    int num;
    /**1 快速加入、2 需要验证加入、3 不可加入； */
    int isAutoJoin;
    /**申请的玩家id集合*/
    List<Long> applyPidList = new ArrayList<>();
    private String declaration; 
    private String creatorName; // 创建者名称


    public GuildMsg.GuildSimpleInfo toProto() {
        GuildMsg.GuildSimpleInfo.Builder builder = GuildMsg.GuildSimpleInfo.newBuilder();
        builder.setId((int) id);
        builder.setName(name);
        builder.setTotalPower(totalPower);
        builder.setIcon(icon);
        builder.setLevel(lv);
        builder.setIsAutoJoin(isAutoJoin);
        builder.setMemberNum(num);
        builder.setDeclaration(declaration); 
        builder.setCreatorName(creatorName); 
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


	public String getDeclaration() {
		return declaration;
	}


	public void setDeclaration(String declaration) {
		this.declaration = declaration;
	}


	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}
	
    
}
