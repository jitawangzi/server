package cn.game.games.cache.entity;

import cn.game.games.cache.base.DbEntity;
import java.io.Serializable;
import java.util.Date;

public class SqlUpdates implements Serializable, DbEntity {
    /**
     * @mbg.generated
     */
    private Integer id;
    /**
     * @mbg.generated
     */
    private String sqlHash;
    /**
     * @mbg.generated
     */
    private Date executeTime;
    /**
     * @mbg.generated
     */
    private String sqlContent;
    /**
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;
    /**
     * @mbg.generated
     */
    public Integer getId() {
        return id;
    }
    /**
     * @mbg.generated
     */
    public void setId(Integer id) {
        this.id = id;
    }
    /**
     * @mbg.generated
     */
    public String getSqlHash() {
        return sqlHash;
    }
    /**
     * @mbg.generated
     */
    public void setSqlHash(String sqlHash) {
        this.sqlHash = sqlHash;
    }
    /**
     * @mbg.generated
     */
    public Date getExecuteTime() {
        return executeTime;
    }
    /**
     * @mbg.generated
     */
    public void setExecuteTime(Date executeTime) {
        this.executeTime = executeTime;
    }
    /**
     * @mbg.generated
     */
    public String getSqlContent() {
        return sqlContent;
    }
    /**
     * @mbg.generated
     */
    public void setSqlContent(String sqlContent) {
        this.sqlContent = sqlContent;
    }
    /**
     * @mbg.generated
     */
    @Override
    public Class<?> getMapperClass() {
        return cn.game.games.net.data.mapper.SqlUpdatesMapper.class;
    }
    /**
     * @mbg.generated
     */
    @Override
    public Object primaryKey() {
        return id ;
    }
}