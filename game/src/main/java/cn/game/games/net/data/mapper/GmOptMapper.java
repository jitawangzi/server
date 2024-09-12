package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.GmOpt;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface GmOptMapper {
    /**
     * @mbg.generated
     */
    int deleteByPrimaryKey(Integer id);
    /**
     * @mbg.generated
     */
    int insert(GmOpt row);
    /**
     * @mbg.generated
     */
    int insertSelective(GmOpt row);
    /**
     * @mbg.generated
     */
    int insertOrUpdate(GmOpt row);
    /**
     * @mbg.generated
     */
    GmOpt selectByPrimaryKey(Integer id);
    /**
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(GmOpt row);
    /**
     * @mbg.generated
     */
    int updateByPrimaryKeyWithBLOBs(GmOpt row);
    /**
     * @mbg.generated
     */
    int updateByPrimaryKey(GmOpt row);
    /**
     * @mbg.generated
     */
    int insertBatch(List<GmOpt> records);
    /**
     * @mbg.generated
     */
    int deleteBatch(List<GmOpt> records);
    /**
     * @mbg.generated
     */
    int updateBatch(@Param("recordList") List<GmOpt> recordList);
}