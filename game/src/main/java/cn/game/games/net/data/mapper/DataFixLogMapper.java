package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.DataFixLog;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DataFixLogMapper {
    /**
     * @mbg.generated
     */
    int deleteByPrimaryKey(Integer id);
    /**
     * @mbg.generated
     */
    int insert(DataFixLog row);
    /**
     * @mbg.generated
     */
    int insertSelective(DataFixLog row);
    /**
     * @mbg.generated
     */
    int insertOrUpdate(DataFixLog row);
    /**
     * @mbg.generated
     */
    DataFixLog selectByPrimaryKey(Integer id);
    /**
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(DataFixLog row);
    /**
     * @mbg.generated
     */
    int updateByPrimaryKey(DataFixLog row);
    /**
     * @mbg.generated
     */
    List<DataFixLog> selectByFixName(@Param("fixName") String fixName);
    /**
     * @mbg.generated
     */
    List<DataFixLog> selectAll();
    /**
     * @mbg.generated
     */
    List<DataFixLog> getBatch(@Param("offset") int offset, @Param("limit") int limit);
    /**
     * @mbg.generated
     */
    int insertBatch(List<DataFixLog> records);
    /**
     * @mbg.generated
     */
    int deleteBatch(List<DataFixLog> records);
    /**
     * @mbg.generated
     */
    int updateBatch(@Param("recordList") List<DataFixLog> recordList);
}