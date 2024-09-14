package cn.game.games.cache.entity;

import cn.game.games.cache.base.DbEntity;

import java.io.Serializable;

/**
 * @ClassName GmOptData
 *
 * @description:
 * @author: ly
 * @create: 2024-09-12 16:46 @Version 1.0
 */
public class GmOptData implements Serializable, DbEntity {
    private Long id;
    private String optPid;
    private String createTime;
    private String optMsg;
    private String optParam;
    private String optResult;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOptPid() {
        return optPid;
    }

    public void setOptPid(String optPid) {
        this.optPid = optPid;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getOptMsg() {
        return optMsg;
    }

    public void setOptMsg(String optMsg) {
        this.optMsg = optMsg;
    }

    public String getOptParam() {
        return optParam;
    }

    public void setOptParam(String optParam) {
        this.optParam = optParam;
    }

    public String getOptResult() {
        return optResult;
    }

    public void setOptResult(String optResult) {
        this.optResult = optResult;
    }

    @Override
    public Object primaryKey() {
        return id;
    }
}
