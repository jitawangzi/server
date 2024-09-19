package cn.game.login.net.clientpacket.vertx.gm;

import cn.game.games.cache.entity.IpWhitelist;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.util.DAO;
import cn.game.util.DateUtil;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @ClassName IpWhitelistManger
 *
 * @description: IP 白名单
 * @author: ly
 * @create: 2024-09-19 15:03 @Version 1.0
 */
public class IpWhitelistManger {
    private final static IpWhitelistManger instance = new IpWhitelistManger();
    List<IpWhitelist> ipWhitelistList = new CopyOnWriteArrayList<>();

    private IpWhitelistManger() {
    }
    public static IpWhitelistManger getInstance() {
        return instance;
    }

    public boolean addIpWhitelist(String ip, int failTimer)  {
        for (IpWhitelist ipWhitelist : ipWhitelistList) {
            if (ipWhitelist.getIp().equals(ip)) {
                return false;
            }
        }
        long expireTime = System.currentTimeMillis();
        if (failTimer <= 0){
            expireTime += DateUtil.DAY_MILLIS * 3650;
        } else {
            expireTime += DateUtil.SECOND_MILLIS * failTimer;
        }
        IpWhitelist ipWhitelist = new IpWhitelist();
        ipWhitelist.setIp(ip);
        ipWhitelist.setFailTimer(new Date(expireTime));
        DAO.insert(ipWhitelist).onSuccess(result -> {
            ipWhitelistList.add(ipWhitelist);
            //TODO  RPC 通知其他 login 节点
        }).onFailure(throwable -> {
            throwable.printStackTrace();
        });
        return true;
    }


    public boolean isIpWhitelist(String ip) {
        long now = System.currentTimeMillis();
        for (IpWhitelist ipWhitelist : ipWhitelistList) {
            if (ipWhitelist.getIp().equals(ip) && now <  ipWhitelist.getFailTimer().getTime()) {
                return true;
            }
        }
        return false;
    }

    public boolean delIpWhitelist(String ip) {
        for (IpWhitelist ipWhitelist : ipWhitelistList) {
            if (ipWhitelist.getIp().equals(ip)) {
                ipWhitelistList.remove(ipWhitelist);
                DAO.delete(ipWhitelist).onSuccess(result -> {;
                    //TODO  RPC 通知其他 login 节点
                }).onFailure(throwable -> {
                    throwable.printStackTrace();
                });
                return true;
            }
        }
        return false;
    }

    public List<IpWhitelist> getIpWhitelistList() {
        return ipWhitelistList;
    }
    
    public void init(){
        refreshIpWhitelistList();
    }

    public void refreshIpWhitelistList() {
        DAO.execute(IpWhitelist.class, MapperConstant.selectAll).onSuccess(result -> {
            List<IpWhitelist> allList = (List<IpWhitelist>) result;
            if (allList != null){
                long now = System.currentTimeMillis();
                allList.removeIf(ipWhitelist -> {
                    boolean delFlag = ipWhitelist.getFailTimer().getTime() < now;
                    if (delFlag){
                        DAO.delete(ipWhitelist);
                    }
                    return delFlag;
                });
                ipWhitelistList.clear();
                this.ipWhitelistList.addAll(allList);
            }
        }).onFailure(throwable -> {
            throwable.printStackTrace();
        });
    }
}
