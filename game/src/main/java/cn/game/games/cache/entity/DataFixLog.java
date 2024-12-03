package cn.game.games.cache.entity;

import cn.game.games.cache.base.DbEntity;
import java.io.Serializable;
import java.util.Date;

public class DataFixLog implements Serializable, DbEntity {
    /**
     * @mbg.generated
     */
    private Integer id;
    /**
     * @mbg.generated
     */
    private String fixName;
    /**
     * @mbg.generated
     */
    private Date executedAt;
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
    public String getFixName() {
        return fixName;
    }
    /**
     * @mbg.generated
     */
    public void setFixName(String fixName) {
        this.fixName = fixName;
    }
    /**
     * @mbg.generated
     */
    public Date getExecutedAt() {
        return executedAt;
    }
    /**
     * @mbg.generated
     */
    public void setExecutedAt(Date executedAt) {
        this.executedAt = executedAt;
    }
    /**
     * @mbg.generated
     */
    @Override
    public Class<?> getMapperClass() {
        return cn.game.games.net.data.mapper.DataFixLogMapper.class;
    }
    /**
     * @mbg.generated
     */
    @Override
    public Object primaryKey() {
        return id ;
    }
}