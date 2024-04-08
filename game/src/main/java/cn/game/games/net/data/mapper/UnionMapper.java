package cn.game.games.net.data.mapper;

import java.util.List;

import cn.game.games.cache.entity.Union;
import org.apache.ibatis.annotations.Param;



public interface UnionMapper {
    /**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int insert(Union row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Union row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Union row);

	/**
	 * @mbg.generated
	 */
	Union selectByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Union row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeyWithBLOBs(Union row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Union row);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<Union> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<Union> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<Union> recordList);

	List<Union> selectAll();
    
    int updateBaseInfo(Union union);
    
    int updateApplys(Union union);
    
    int updateJournals(Union union);
    
    int updateUpgrading(Union union);
    
    int updateItems(Union union);
}