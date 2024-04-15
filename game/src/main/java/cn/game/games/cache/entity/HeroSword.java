package cn.game.games.cache.entity;

import cn.game.games.cache.base.DbEntity;
import java.io.Serializable;

public class HeroSword implements Serializable, DbEntity {
    /**
     * @mbg.generated
     */
    private Long id;
    /**
     * @mbg.generated
     */
    private Long playerId;
    /**
     * 武器配置表id
     * @mbg.generated
     */
    private Integer configId;
    /**
     * 武器星级
     * @mbg.generated
     */
    private Integer star;
    /**
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;
    /**
     * @mbg.generated
     */
    public Long getId() {
        return id;
    }
    /**
     * @mbg.generated
     */
    public void setId(Long id) {
        this.id = id;
    }
    /**
     * @mbg.generated
     */
    public Long getPlayerId() {
        return playerId;
    }
    /**
     * @mbg.generated
     */
    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }
    /**
     * @mbg.generated
     */
    public Integer getConfigId() {
        return configId;
    }
    /**
     * @mbg.generated
     */
    public void setConfigId(Integer configId) {
        this.configId = configId;
    }
    /**
     * @mbg.generated
     */
    public Integer getStar() {
        return star;
    }
    /**
     * @mbg.generated
     */
    public void setStar(Integer star) {
        this.star = star;
    }
    /**
     * @mbg.generated
     */
    @Override
    public Class<?> getMapperClass() {
        return cn.game.games.net.data.mapper.HeroSwordMapper.class;
    }
    /**
     * @mbg.generated
     */
    @Override
    public Object primaryKey() {
        return id ;
    }
}