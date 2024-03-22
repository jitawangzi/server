package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.PlayerExtMapper;
import cn.game.games.net.game.helper.EventHelper;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.OldGlobalConst;
import cn.game.protocol.generated.config.StrategyCardConfig;
import cn.game.protocol.generated.manager.StrategyCardManager;
import cn.game.protocol.protobuf.BuildingMsg;
import cn.game.util.KryoUtils;
import cn.game.util.Rnd;
import cn.game.util.StrUtil;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class PlayerExt implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	private Long playerId;

	/**
	 * 已经领取过的活跃任务奖励索引
	 * @mbg.generated
	 */
	private Integer questActive;

	/**
	 * 上一次打探索副本使用的角色
	 * @mbg.generated
	 */
	private String lastExploreRole;

	/**
	 * 当前设置的支线任务优先分组
	 * @mbg.generated
	 */
	private Integer branchGroup;

	/**
	 * 主城buff
	 * @mbg.generated
	 */
	private String mainCityBuffs;

	/**
	 * 策略卡组id
	 * @mbg.generated
	 */
	private Long strategycardGroupId;

	/**
	 * 产生的尚未选择选项的事件
	 * @mbg.generated
	 */
	private String eventIds;

	/**
	 * 主城添加奖励时间
	 * @mbg.generated
	 */
	private Long buildingAddItemTime;

	/**
	 * 主城建筑激活的天赋节点
	 * @mbg.generated
	 */
	private byte[] occtalentNode;

	/**
	 * 图鉴
	 * @mbg.generated
	 */
	private byte[] illustrate;

	/**
	 * 科技树激活的节点
	 * @mbg.generated
	 */
	private byte[] technologyTreeNode;

	/**
	 * 获得的策略卡
	 * @mbg.generated
	 */
	private byte[] strategyCards;

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
	public Integer getQuestActive() {
		return questActive;
	}

	/**
	 * @mbg.generated
	 */
	public void setQuestActive(Integer questActive) {
		this.questActive = questActive;
	}

	/**
	 * @mbg.generated
	 */
	public String getLastExploreRole() {
		return lastExploreRole;
	}

	/**
	 * @mbg.generated
	 */
	public void setLastExploreRole(String lastExploreRole) {
		this.lastExploreRole = lastExploreRole;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getBranchGroup() {
		return branchGroup;
	}

	/**
	 * @mbg.generated
	 */
	public void setBranchGroup(Integer branchGroup) {
		this.branchGroup = branchGroup;
	}

	/**
	 * @mbg.generated
	 */
	public String getMainCityBuffs() {
		return mainCityBuffs;
	}

	/**
	 * @mbg.generated
	 */
	public void setMainCityBuffs(String mainCityBuffs) {
		this.mainCityBuffs = mainCityBuffs;
	}

	/**
	 * @mbg.generated
	 */
	public Long getStrategycardGroupId() {
		return strategycardGroupId;
	}

	/**
	 * @mbg.generated
	 */
	public void setStrategycardGroupId(Long strategycardGroupId) {
		this.strategycardGroupId = strategycardGroupId;
	}

	/**
	 * @mbg.generated
	 */
	public String getEventIds() {
		return eventIds;
	}

	/**
	 * @mbg.generated
	 */
	public void setEventIds(String eventIds) {
		this.eventIds = eventIds;
	}

	/**
	 * @mbg.generated
	 */
	public Long getBuildingAddItemTime() {
		return buildingAddItemTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setBuildingAddItemTime(Long buildingAddItemTime) {
		this.buildingAddItemTime = buildingAddItemTime;
	}

	/**
	 * @mbg.generated
	 */
	public byte[] getOcctalentNode() {
		return occtalentNode;
	}

	/**
	 * @mbg.generated
	 */
	public void setOcctalentNode(byte[] occtalentNode) {
		this.occtalentNode = occtalentNode;
	}

	/**
	 * @mbg.generated
	 */
	public byte[] getIllustrate() {
		return illustrate;
	}

	/**
	 * @mbg.generated
	 */
	public void setIllustrate(byte[] illustrate) {
		this.illustrate = illustrate;
	}

	/**
	 * @mbg.generated
	 */
	public byte[] getTechnologyTreeNode() {
		return technologyTreeNode;
	}

	/**
	 * @mbg.generated
	 */
	public void setTechnologyTreeNode(byte[] technologyTreeNode) {
		this.technologyTreeNode = technologyTreeNode;
	}

	/**
	 * @mbg.generated
	 */
	public byte[] getStrategyCards() {
		return strategyCards;
	}

	/**
	 * @mbg.generated
	 */
	public void setStrategyCards(byte[] strategyCards) {
		this.strategyCards = strategyCards;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.PlayerExtMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return playerId;
	}

	public static PlayerExt valueOf(long playerId) {
		PlayerExt playerExt = new PlayerExt();
		playerExt.setPlayerId(playerId);
		return playerExt;
	}

	/**主城激活的天赋节点**/
	private transient List<Integer> occTalentNodes = new ArrayList<>();

	/**图鉴**/
	private transient Map<Integer, List<Integer>> id_illustrates = new HashMap<>();

	private transient Map<Integer, Integer> mainCityBuffMap = new HashMap<>();

	/**科技树激活的节点**/
	private transient List<Integer> id_technologyTreeNodes = new ArrayList<>();

	/**策略卡*/
	private transient List<Integer> id_strategyCards = new ArrayList<>();

	/**产生的事件id*/
	private transient List<Integer> id_events = new ArrayList<>();


	public List<Integer> getOccTalentNodes() {
		if (occTalentNodes.isEmpty() && occtalentNode != null && occtalentNode.length != 0) {
			occTalentNodes = KryoUtils.deserializeWithVersion(occtalentNode, ArrayList.class);
		}
		return occTalentNodes;
	}

	/**
	 * 添加激活的天赋
	 * @param id
	 */
	public void addOccTalentNode(int id) {
		getOccTalentNodes();
		if (!this.occTalentNodes.contains(id)) {
			this.occTalentNodes.add(id);
		}
		this.occtalentNode = KryoUtils.serializeWithVersion(occTalentNodes);
	}

	public List<Integer> getIllustrateByType(int type) {
		if (this.id_illustrates.isEmpty() && this.illustrate != null && this.illustrate.length != 0) {
			this.id_illustrates = parseIllustrate(this.illustrate);
		}

		return this.id_illustrates.get(type) == null ? new ArrayList<>() : this.id_illustrates.get(type);
	}

	/**
	 * 添加图鉴
	 * @param type
	 * @param configId
	 */
	public void addIllustrate(int type, int configId) {
		List<Integer> illustrateByType = getIllustrateByType(type);
		if (!illustrateByType.contains(configId)) {
			illustrateByType.add(configId);
			this.id_illustrates.put(type, illustrateByType);
			this.illustrate = makeIllustrateInfo().build().toByteArray();
			PlayerExt update = PlayerExt.valueOf(playerId);
			update.setIllustrate(this.illustrate);
			DAO.updateSelective(PlayerExtMapper.class, update);
			//触发事件
			EventHelper.handleEvent(playerId, new GameEvent(EventTypeEnum.CollectAtlas));
		}
	}

	public BuildingMsg.Illustrate.Builder makeIllustrateInfo() {
		BuildingMsg.Illustrate.Builder builder = BuildingMsg.Illustrate.newBuilder();
		for (Map.Entry<Integer, List<Integer>> map : id_illustrates.entrySet()) {
			BuildingMsg.IllustrateInfo.Builder list = BuildingMsg.IllustrateInfo.newBuilder();
			list.setType(BuildingMsg.IllustrateType.forNumber(map.getKey()));
			if (map.getValue() != null && map.getValue().size() > 0) {
				list.addAllIds(map.getValue());
			}
			builder.addInfos(list);
		}
		return builder;
	}

	public Map<Integer, List<Integer>> parseIllustrate(byte[] data){
		try {
			BuildingMsg.Illustrate illustrate = BuildingMsg.Illustrate.parseFrom(data);
			Map<Integer, List<Integer>> id_illustrates = new HashMap<>();
			for (BuildingMsg.IllustrateInfo info : illustrate.getInfosList()) {
				int type = info.getType().getNumber();
				List<Integer> list = id_illustrates.get(type);
				if (list == null) {
					list = new ArrayList<>();
				}
				list.addAll(info.getIdsList());
				id_illustrates.put(type, list);
			}
			return id_illustrates;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new HashMap<>();
	}

	public Map<Integer, Integer> getMainCityBuffMap() {
		if (mainCityBuffMap.isEmpty() && !StringUtils.isEmpty(this.mainCityBuffs)) {
			mainCityBuffMap = StrUtil.toMap(this.mainCityBuffs);
		}
		return mainCityBuffMap;
	}


	public List<Integer> getTechnologyTreeNodes() {
		if (id_technologyTreeNodes.isEmpty() && technologyTreeNode != null && technologyTreeNode.length != 0) {
			id_technologyTreeNodes = KryoUtils.deserializeWithVersion(technologyTreeNode, ArrayList.class);
		}
		return id_technologyTreeNodes;
	}

	/**
	 * 添加激活的科技树节点
	 * @param id
	 */
	public void addTechnologyTreeNode(int id) {
		getTechnologyTreeNodes();
		if (!this.id_technologyTreeNodes.contains(id)) {
			this.id_technologyTreeNodes.add(id);
		}
		this.technologyTreeNode = KryoUtils.serializeWithVersion(id_technologyTreeNodes);
	}

	public List<Integer> getStrategyCardList() {
		if (id_strategyCards.isEmpty() && strategyCards != null && strategyCards.length != 0) {
			id_strategyCards = KryoUtils.deserializeWithVersion(strategyCards, ArrayList.class);
		}
		return id_strategyCards;
	}

	/**
	 * 添加策略卡
	 * @param id
	 */
	public void addStrategyCard(int id) {
		if (!this.id_strategyCards.contains(id)) {
			this.id_strategyCards.add(id);
		}
		this.strategyCards = KryoUtils.serializeWithVersion(id_strategyCards);
	}

	/**
	 * 添加初始策略卡
	 */
	public void addInitStrategyCard() {
		for (Integer card : OldGlobalConst.initStrategyCards) {
			if (this.id_strategyCards.contains(card)) {
				continue;
			}
			StrategyCardConfig config = StrategyCardManager.getInstance().getStrategyCardConfig(card);
			addStrategyCard(config.getId());
		}
		PlayerExt update = PlayerExt.valueOf(playerId);
		update.setStrategyCards(strategyCards);
		DAO.updateSelective(PlayerExtMapper.class, update);
	}

	/**
	 * 从玩家未拥有的策略卡中依据各自的权重随机一张策略卡
	 */
	public int randomStrategyCardFromTable() {
		Collection<StrategyCardConfig> list = StrategyCardManager.getInstance().list();
		List<StrategyCardConfig> collect = list.stream().filter(e -> !this.id_strategyCards.contains(e.getId())).collect(Collectors.toList());
		if (collect == null || collect.size() == 0) {
			return 0;
		}
		int index = Rnd.randomWeighableIndex(collect);
		StrategyCardConfig config = collect.get(index);
		return config.getId();
	}

	/**
	 * 添加事件id
	 * @param id
	 */
	public void addEventId(int id) {
		getEventIdList();
		if (this.id_events.contains(id)) {
			return;
		}
		this.id_events.add(id);
		this.eventIds = StrUtil.toString(this.id_events);
	}

	public List<Integer> getEventIdList() {
		if (this.id_events.isEmpty() && this.eventIds != null) {
			this.id_events = StrUtil.toList(this.eventIds);
		}
		return this.id_events;
	}

	public void removeEventId(int id) {
		getEventIdList();
		if (!this.id_events.contains(id)) {
			return;
		}
		Iterator<Integer> iterator = this.id_events.iterator();
		while (iterator.hasNext()) {
			int next = iterator.next();
			if (next == id) {
				iterator.remove();
				break;
			}
		}
		this.eventIds = StrUtil.toString(id_events);
	}


}