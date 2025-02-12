package cn.game.games.net.cross.zongmen;

import cn.game.protocol.generated.manager.GuildIconManager;
import cn.game.protocol.protobuf.ZongMenMsg;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName ZongMenSetting
 *
 * @description: 宗门设置相关
 * @author: ly
 * @create: 2025-02-08 14:37 @Version 1.0
 */
public class ZongMenSetting implements ZongMenConstants.ZongMenEventHandler {
    /**宗主微信号*/
    String wx;
    /**微信号修改次数 */
    int wxChangeNum;
    /** 解锁的图标 */
    List<Integer> unlockIconList = new ArrayList<>();
    /**上一次修改宗门名称时间戳*/
    long lastChangeNameTimer;
    /**上一次修改微信号时间戳*/
    long lastChangeWxTimer;
    /**自动加入*/
    boolean autoJoin;
    /*** 天道等级 */
    int tianDaoLevel;


    @Override
    public ZongMenConstants.ZongMenEvenType[] getRegisterEvent() {
        return new ZongMenConstants.ZongMenEvenType[]{ZongMenConstants.ZongMenEvenType.ZONG_MEN_LEVEL_UP};
    }

    @Override
    public void handleEventType(ZongMenConstants.ZongMenEvenType type, ZongMenInfo info, Object... params) {
        switch (type) {
            case ZONG_MEN_LEVEL_UP:
                //宗门等级提升
                int level = info.getLv();
                GuildIconManager.instance().list().stream().filter(icon -> icon.LV == level).forEach(icon -> {
                    for (int iconId : icon.Icon)  {
                        if (unlockIconList.contains(iconId)) {
                            continue;
                        }
                        unlockIconList.add(iconId);
                    }
                });
                break;
            default:
                break;
        }
    }

    public ZongMenMsg.ZongMenSettingProto.Builder toProto(){
        ZongMenMsg.ZongMenSettingProto.Builder builder = ZongMenMsg.ZongMenSettingProto.newBuilder();
        builder.setWx(wx);
        builder.setWxChangeNum(wxChangeNum);
        builder.addAllIconList(unlockIconList);
        builder.setLastChangeNameTimer((int) (lastChangeNameTimer/1000L));
        builder.setLastChangeWxTimer((int) (lastChangeWxTimer/1000L));
        return builder;
    }

    public String getWx() {
        return wx;
    }

    public void setWx(String wx) {
        this.wx = wx;
    }

    public int getWxChangeNum() {
        return wxChangeNum;
    }

    public void setWxChangeNum(int wxChangeNum) {
        this.wxChangeNum = wxChangeNum;
    }

    public List<Integer> getUnlockIconList() {
        return unlockIconList;
    }

    public void setUnlockIconList(List<Integer> unlockIconList) {
        this.unlockIconList = unlockIconList;
    }

    public long getLastChangeNameTimer() {
        return lastChangeNameTimer;
    }

    public void setLastChangeNameTimer(long lastChangeNameTimer) {
        this.lastChangeNameTimer = lastChangeNameTimer;
    }

    public long getLastChangeWxTimer() {
        return lastChangeWxTimer;
    }

    public void setLastChangeWxTimer(long lastChangeWxTimer) {
        this.lastChangeWxTimer = lastChangeWxTimer;
    }

    public boolean isAutoJoin() {
        return autoJoin;
    }

    public void setAutoJoin(boolean autoJoin) {
        this.autoJoin = autoJoin;
    }

    public int getTianDaoLevel() {
        return tianDaoLevel;
    }

    public void setTianDaoLevel(int tianDaoLevel) {
        this.tianDaoLevel = tianDaoLevel;
    }
}
