package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.SqlUpdates;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SqlUpdatesMapper {
    /**
     * @mbg.generated
     */
    int deleteByPrimaryKey(Integer id);
    /**
     * @mbg.generated
     */
    int insert(SqlUpdates row);
    /**
     * @mbg.generated
     */
    int insertSelective(SqlUpdates row);
    /**
     * @mbg.generated
     */
    int insertOrUpdate(SqlUpdates row);
    /**
     * @mbg.generated
     */
    SqlUpdates selectByPrimaryKey(Integer id);
    /**
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(SqlUpdates row);
    /**
     * @mbg.generated
     */
    int updateByPrimaryKeyWithBLOBs(SqlUpdates row);
    /**
     * @mbg.generated
     */
    int updateByPrimaryKey(SqlUpdates row);
    /**
     * @mbg.generated
     */
    List<SqlUpdates> selectByUniqueSql(@Param("sqlHash") String sqlHash);
    /**
     * @mbg.generated
     */
    List<SqlUpdates> selectAll();
    /**
     * @mbg.generated
     */
    List<SqlUpdates> getBatch(@Param("offset") int offset, @Param("limit") int limit);
    /**
     * @mbg.generated
     */
    int insertBatch(List<SqlUpdates> records);
    /**
     * @mbg.generated
     */
    int deleteBatch(List<SqlUpdates> records);
    /**
     * @mbg.generated
     */
    int updateBatch(@Param("recordList") List<SqlUpdates> recordList);
}