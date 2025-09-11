package cn.game.games.net.game.module.develop.gem;

import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.cache.base.DbEntity;
import cn.game.games.cache.entity.ItemNoStack;
import cn.game.protocol.generated.config.EntryEffectConfig;
import cn.game.protocol.generated.enume.EntryEffectEnum;
import cn.game.protocol.generated.manager.EntryEffectManager;
import cn.game.protocol.protobuf.BaseMsg.GemInfo;

public class Gem extends ItemNoStack implements Serializable, DbEntity {

	private boolean isLock;

	@Deprecated
	@JsonIgnore
	private Map<Integer, Integer> gemAttrs = new HashMap<Integer, Integer>();
	@Deprecated
	@JsonIgnore
	private List<Integer> gemSkills = new ArrayList<>();
	
	private List<Integer> entryEffectList = new ArrayList<>();


	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	public boolean isLock() {
		return isLock;
	}

	public void setLock(boolean isLock) {
		this.isLock = isLock;
	}
	

	/** 
	 * 获取增加的属性
	 * @return
	 */
	public List<Integer> getAttrList() {
		List<Integer> attrList = entryEffectList.stream().filter(r->{
			EntryEffectConfig entryEffectConfig = EntryEffectManager.instance().get(r); 
			return entryEffectConfig.type == EntryEffectEnum.AttributeChange.ID ;
		}).collect(toList()); 
		return attrList; 
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}

	public GemInfo toGemInfo() {
		return GemInfo.newBuilder().setUid(getId() + "").setConfigId(getConfigId()).setIsLock(isLock).addAllEntryEffectIds(entryEffectList).build();
	}
}