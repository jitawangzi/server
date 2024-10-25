package cn.game.login.net.clientpacket.vertx.gm;

import cn.game.core.net.vertx.VxHolder;
import cn.game.login.cache.entity.IpWhitelist;
import cn.game.login.mapper.IpWhitelistMapper;
import cn.game.login.net.handler.LoginServerHandler;
import cn.game.protocol.protobuf.ServerMsg;
import cn.game.util.DateUtil;
import cn.game.util.ServerType;
import cn.game.util.SpringContextLoader;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @ClassName IpWhitelistManger
 * @description: IP 白名单
 * @author: ly
 * @create: 2024-09-19 15:03 @Version 1.0
 */
public class IpWhitelistManger {
    private final static IpWhitelistManger instance = new IpWhitelistManger();
    List<IpWhitelist> ipWhitelistList = new CopyOnWriteArrayList<>();
    IpWhitelistMapper mapper;

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
        ipWhitelist.setCreateTimer(new Date());
        ipWhitelist.setFailTimer(new Date(expireTime));

        mapper.insert(ipWhitelist);
        refreshIpWhitelistList();
        //  RPC 通知其他 login 节点
        VxHolder.broadcastRemoteServer(ServerType.Login, ServerMsg.LoginUpdateGmInfoRequest_7d000076.newBuilder().setType(LoginServerHandler.UPDATE_WHITE_LIST).build());

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
                mapper.deleteByPrimaryKey(ipWhitelist.getId());
                //  RPC 通知其他 login 节点
                VxHolder.broadcastRemoteServer(ServerType.Login, ServerMsg.LoginUpdateGmInfoRequest_7d000076.newBuilder().setType(LoginServerHandler.UPDATE_WHITE_LIST).build());
                return true;
            }
        }
        return false;
    }

    public List<IpWhitelist> getIpWhitelistList() {
        return ipWhitelistList;
    }
    
    public void init(){
        mapper = SpringContextLoader.getContext().getBean(IpWhitelistMapper.class);
        refreshIpWhitelistList();

    }

    public void refreshIpWhitelistList() {
        List<IpWhitelist> allList = mapper.selectAll();
        if (allList != null){
            long now = System.currentTimeMillis();
            allList.removeIf(ipWhitelist -> {
                boolean delFlag = ipWhitelist.getFailTimer().getTime() < now;
                if (delFlag){
                    mapper.deleteByPrimaryKey(ipWhitelist.getId());
                }
                return delFlag;
            });
            ipWhitelistList.clear();
            this.ipWhitelistList.addAll(allList);
        }
    }
}
