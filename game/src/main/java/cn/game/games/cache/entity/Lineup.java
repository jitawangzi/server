package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import cn.game.util.StrUtil;

/**
 * t_lineup
 * @author
 */
public class Lineup implements Serializable {
    /**
	 * 角色id
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 阵容id TeamConfigure表的id
	 * @mbg.generated
	 */
	private Integer lineupId;
	/**
	 * 阵容里的伙伴位置数据 格式 roleid:position| roleid:position
	 * @mbg.generated
	 */
	private String postions;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

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
	public Integer getLineupId() {
		return lineupId;
	}

	/**
	 * @mbg.generated
	 */
	public void setLineupId(Integer lineupId) {
		this.lineupId = lineupId;
	}

	/**
	 * @mbg.generated
	 */
	public String getPostions() {
		return postions;
	}

	/**
	 * @mbg.generated
	 */
	public void setPostions(String postions) {
		this.postions = postions;
	}

	public void setPostionInfo(String postions) {
		this.postions = postions;
		rolepos = StrUtil.toMap(this.postions);
	}

	/**
	 * key:位置  value:roleId
	 */
	private transient Map<Integer,Integer> rolepos = new HashMap<>();

    public Map<Integer,Integer> getRolepos() {
        if (rolepos.isEmpty() && !StringUtils.isEmpty(this.postions)) {
            rolepos = StrUtil.toMap(this.postions);
        }
        return rolepos;
    }
    
    public Map<Integer,Integer> getRoleposO() {
        return rolepos;
    }
    
    /**
     * 获取角色在编队中的位置
     * @param roleId
     * @return 不在编队中返回-1
     */
	public int getRolepos(int roleId) {
		Map<Integer, Integer> pos_role = getRolepos();
		for (Map.Entry<Integer, Integer> entry : pos_role.entrySet()) {
			int pos = entry.getKey();
			int id = entry.getValue();
			if (id == roleId) {
				return pos;
			}
		}
		return -1;
	}
    
}