package cn.game.games.net.cross.zongmen;

import java.util.HashMap;
import java.util.Map;

import cn.game.core.cache.CacheType;
import cn.game.core.cache.RedisLocalCache;
import cn.game.protocol.generated.manager.GuildIconManager;
import cn.game.protocol.protobuf.ZongMenMsg;
import cn.game.util.LockUtil;
import io.vertx.core.Future;
import io.vertx.core.Promise;

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

    /** 解锁的图标 key iconId ,value 过期时间戳 -1 永久 */
	public Map<Integer, Long> unlockIconMap = new HashMap<>();

    /**上一次修改宗门名称时间戳*/
    long lastChangeNameTimer;
    /**上一次修改微信号时间戳*/
    long lastChangeWxTimer;
    /** 1 快速加入、2 需要验证加入、3 不可加入； */
    int autoJoin;
    /*** 天道等级 */
    int tianDaoLevel;

    @Override
    public ZongMenConstants.ZongMenEvenType[] getRegisterEvent() {
        return new ZongMenConstants.ZongMenEvenType[]{ZongMenConstants.ZongMenEvenType.ZONG_MEN_LEVEL_UP};
    }

    @Override
    public void handleEventType(ZongMenConstants.ZongMenEvenType type, ZongMen info, Object... params) {
        switch (type) {
            case ZONG_MEN_LEVEL_UP:
                //宗门等级提升
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
     * 修改宗门名称
     * @param zongMenInfo 宗门信息
     * @param newName 新名称
     * @param operatorName 操作者名称
     * @return 是否成功
     */
    public Future<Boolean> changeZongmenName(ZongMen zongMenInfo, String newName, String operatorName) {
        Promise<Boolean> promise = Promise.promise();
        
        RedisLocalCache.getInstance().getAsync(CacheType.ZONG_MEN_NAME_ID.key(newName)).onSuccess((result) -> {
            if (result == null) { // 该名称未被占用
                boolean redisLock = LockUtil.tryLockNoWaitSync(6, CacheType.ZONG_MEN_NAME_CHANGE_LOCK.key(newName));
                if (redisLock) {
                    // 删除旧的宗门名称id映射
                    zongMenInfo.delZongMenNameIdRedisData();
                    zongMenInfo.getData().setName(newName);
                    // 名称修改：玩家昵称修改宗门名称为宗门昵称
                    zongMenInfo.handleEvent(ZongMenConstants.ZongMenEvenType.CHANGE_ZONG_MEN_NAME, operatorName, newName);
                    // 保存新的宗门名称id映射
                    ZongMenManager.getInstance().saveRedisNameIdMap(newName, zongMenInfo.getId());
                    this.lastChangeNameTimer = System.currentTimeMillis();
                    promise.complete(true);
                } else {
                    promise.complete(false);
                }
            } else { // 该名称被占用
                promise.complete(false);
            }
        }).onFailure(err -> {
            err.printStackTrace();
            promise.complete(false);
        });
        
        return promise.future();
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
     * @param zongMenInfo 宗门信息
     * @param notice 公告内容
     * @param operatorName 操作者名称
     */
    public void changeNotice(ZongMen zongMenInfo, String notice, String operatorName) {
        zongMenInfo.getData().setNotice(notice);
        // 公告修改：玩家昵称修改了公告
        zongMenInfo.handleEvent(ZongMenConstants.ZongMenEvenType.CHANGE_ZONG_MEN_NOTICE, operatorName);
    }

    /**
     * 修改宣言
     * @param zongMenInfo 宗门信息
     * @param declaration 宣言内容
     * @param operatorName 操作者名称
     */
    public void changeDeclaration(ZongMen zongMenInfo, String declaration, String operatorName) {
        zongMenInfo.getData().setDeclaration(declaration);
        // 宣言修改：玩家昵称修改了宣言
        zongMenInfo.handleEvent(ZongMenConstants.ZongMenEvenType.CHANGE_ZONG_MEN_DECLARATION, operatorName);
    }

    /**
     * 修改图标
     * @param zongMenInfo 宗门信息
     * @param icon 图标ID
     */
    public void changeIcon(ZongMen zongMenInfo, int icon) {
        zongMenInfo.getData().setIcon(icon);
    }

    public ZongMenMsg.ZongMenSettingProto.Builder toProto(){
        ZongMenMsg.ZongMenSettingProto.Builder builder = ZongMenMsg.ZongMenSettingProto.newBuilder();
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

