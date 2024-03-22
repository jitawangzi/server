package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.Lead;

public interface LeadMapper {
    int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("leadId") Byte leadId);
    int deleteByPlayerId(Long playerId) ; 

    int insert(Lead record);

    int insertSelective(Lead record);

    Lead selectByPrimaryKey(@Param("playerId") Long playerId, @Param("leadId") Byte leadId);
    
    List<Lead> selectAll(Long playerId) ; 

    int updateByPrimaryKeySelective(Lead record);

    int updateByPrimaryKey(Lead record);
}