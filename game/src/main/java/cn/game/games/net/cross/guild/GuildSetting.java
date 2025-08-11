package cn.game.games.net.cross.guild;

import java.util.HashMap;
import java.util.Map;

import cn.game.core.cache.CacheType;
import cn.game.core.cache.RedisLocalCache;
import cn.game.protocol.generated.manager.GuildIconManager;
import cn.game.protocol.protobuf.GuildMsg;
import cn.game.util.LockUtil;
import cn.game.util.RedisUtil;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * @ClassName GuildSetting
 *
 * @description: 公会设置相关
 * @author: ly
 * @create: 2025-02-08 14:37 @Version 1.0
 */
public class GuildSetting implements GuildConstants.GuildEventHandler {
    /**宗主微信号*/
    String wx;
    /**微信号修改次数 */
    int wxChangeNum;

    /** 解锁的图标 key iconId ,value 过期时间戳 -1 永久 */
	public Map<Integer, Long> unlockIconMap = new HashMap<>();

    /**上一次修改公会名称时间戳*/
    long lastChangeNameTimer;
    /**上一次修改微信号时间戳*/
    long lastChangeWxTimer;
    /** 1 快速加入、2 需要验证加入、3 不可加入； */
    int autoJoin;
    /*** 天道等级 */
    int tianDaoLevel;

    @Override
    public GuildConstants.GuildEvenType[] getRegisterEvent() {
        return new GuildConstants.GuildEvenType[]{GuildConstants.GuildEvenType.ZONG_MEN_LEVEL_UP};
    }

    @Override
    public void handleEventType(GuildConstants.GuildEvenType type, Guild info, Object... params) {
        switch (type) {
            case ZONG_MEN_LEVEL_UP:
                //公会等级提升
                int level = info.getLv();
                GuildIconManager.instance().list().stream().filter(icon -> icon.LV == level).forEach(icon -> {
                    for (int iconId : icon.Icon)  {
                        if (unlockIconMap.containsKey(iconId)) {
                            continue;
                        }
                        unlockIconMap.put(iconId, -1L);
                    }
                });
                break;
            default:
                break;
        }
    }

    /**
     * 修改公会名称
     * @param guildInfo 公会信息
     * @param newName 新名称
     * @param operatorName 操作者名称
     * @return 是否成功
     */
    public boolean changeGuildName(Guild guildInfo, String newName, String operatorName) {
        boolean trySet = RedisUtil.trySet(GuildHelper.getNameKey(newName), newName); 
		if (!trySet) {
			// 名称修改失败,已经存在了
			return false;
		}

        // 删除旧的公会名称id映射
        guildInfo.delGuildNameIdRedisData();
        guildInfo.getData().setName(newName);
        // 名称修改：玩家昵称修改公会名称为公会昵称
        guildInfo.handleEvent(GuildConstants.GuildEvenType.CHANGE_ZONG_MEN_NAME, operatorName, newName);
        this.lastChangeNameTimer = System.currentTimeMillis();
        return true; 
    }

    /**
     * 修改微信号
     * @param wx 微信号
     */
    public void changeWx(String wx) {
        this.wx = wx;
        this.wxChangeNum++;
        this.lastChangeWxTimer = System.currentTimeMillis();
    }

    /**
     * 修改公告
     * @param guildInfo 公会信息
     * @param notice 公告内容
     * @param operatorName 操作者名称
     */
    public void changeNotice(Guild guildInfo, String notice, String operatorName) {
        guildInfo.getData().setNotice(notice);
        // 公告修改：玩家昵称修改了公告
        guildInfo.handleEvent(GuildConstants.GuildEvenType.CHANGE_ZONG_MEN_NOTICE, operatorName);
    }

    /**
     * 修改宣言
     * @param guildInfo 公会信息
     * @param declaration 宣言内容
     * @param operatorName 操作者名称
     */
    public void changeDeclaration(Guild guildInfo, String declaration, String operatorName) {
        guildInfo.getData().setNotification(declaration);
        
        // 宣言修改：玩家昵称修改了宣言
        guildInfo.handleEvent(GuildConstants.GuildEvenType.CHANGE_ZONG_MEN_DECLARATION, operatorName);
    }

    /**
     * 修改图标
     * @param guildInfo 公会信息
     * @param icon 图标ID
     */
    public void changeIcon(Guild guildInfo, int icon) {
        guildInfo.getData().setIcon(icon);
    }

    public GuildMsg.GuildSettingProto.Builder toProto(){
        GuildMsg.GuildSettingProto.Builder builder = GuildMsg.GuildSettingProto.newBuilder();
        builder.setWx(wx == null ? "" : wx);
        builder.setWxChangeNum(wxChangeNum);
        long now = System.currentTimeMillis();
        unlockIconMap.forEach((ionId, expireTime) -> {
                builder.putIconList(ionId,  expireTime > 0 ?  ((int) (expireTime - now/1000L)) : expireTime.intValue());
        });
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

    public Map<Integer, Long> getUnlockIconMap() {
        return unlockIconMap;
    }

    public void setUnlockIconMap(Map<Integer, Long> unlockIconMap) {
        this.unlockIconMap = unlockIconMap;
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

    public int getAutoJoin() {
        return autoJoin;
    }

    public void setAutoJoin(int autoJoin) {
        this.autoJoin = autoJoin;
    }

    public int getTianDaoLevel() {
        return tianDaoLevel;
    }

    public void setTianDaoLevel(int tianDaoLevel) {
        this.tianDaoLevel = tianDaoLevel;
    }
}

