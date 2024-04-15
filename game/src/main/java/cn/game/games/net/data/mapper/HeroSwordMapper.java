package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.HeroSword;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface HeroSwordMapper {
    /**
     * @mbg.generated
     */
    int deleteByPrimaryKey(Long id);
    /**
     * @mbg.generated
     */
    int insert(HeroSword row);
    /**
     * @mbg.generated
     */
    int insertSelective(HeroSword row);
    /**
     * @mbg.generated
     */
    int insertOrUpdate(HeroSword row);
    /**
     * @mbg.generated
     */
    HeroSword selectByPrimaryKey(Long id);
    /**
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(HeroSword row);
    /**
     * @mbg.generated
     */
    int updateByPrimaryKey(HeroSword row);
    /**
     * @mbg.generated
     */
    List<HeroSword> selectByPlayerId(@Param("playerId") Long playerId);
    /**
     * @mbg.generated
     */
    int insertBatch(List<HeroSword> records);
    /**
     * @mbg.generated
     */
    int deleteBatch(List<HeroSword> records);
    /**
     * @mbg.generated
     */
    int updateBatch(@Param("recordList") List<HeroSword> recordList);
}