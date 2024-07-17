package cn.game.games.net.game.manager;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import cn.game.core.util.IdUtil;
import cn.game.core.util.IdUtil.IdType;
import cn.game.games.cache.entity.Member;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.Union;
import cn.game.games.cache.entity.UnionApplication;
import cn.game.games.net.data.mapper.MemberMapper;
import cn.game.games.net.data.mapper.UnionApplicationMapper;
import cn.game.games.net.data.mapper.UnionMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.enume.UnionTitleEnum;
import cn.game.util.ObjUtil;

public class UnionManager {

	private static final Logger log = LoggerFactory.getLogger(UnionManager.class);
	/** 所有工会 */
	private ConcurrentMap<Long, Union> unionsMap = new ConcurrentHashMap<>();
	/** 所有工会 */
	private List<Union> unionsList = new CopyOnWriteArrayList<Union>();
	/** 所有工会成员,只是成员对象，也可能成员不在工会里 */
	private ConcurrentMap<Long, Member> membersMap = new ConcurrentHashMap<>();
	/** 工会-->成员集合（Member中的unionId >0 ） */
	private Multimap<Long, Member> unionMembersMap = ArrayListMultimap.create();
	/** 工会-->申请人集合 */
	private Multimap<Long, Long> unionApplicationsMap = HashMultimap.create();
	/** 所有的工会名集合，可以用来检查公会名重复 */
	private ConcurrentSkipListSet<String> unionNamesSet = new ConcurrentSkipListSet<>();

	private static UnionManager instance = new UnionManager() ; 
	private static final int PAGE_SIZE = 15;


	public static UnionManager getInstance() {
		return instance ; 
	}

	/**
	 * 获取一个玩家的成员数据
	 * @param playerId
	 * @return
	 */
	public Member getMember(long playerId) {
		Member member = this.membersMap.get(playerId);
		return member;
	}
	/**
	 * 获取一个工会的所有成员
	 * @param unionId
	 * @return
	 */
	public Collection<Member> getMembers(long unionId) {
		return this.unionMembersMap.get(unionId);
	}
	/**
	 * 获取工会
	 * @param id
	 * @return
	 */
	public Union getUnion(long id) {
		return this.unionsMap.get(id);
	}

	public void init() {
	

//		List<Member> members = (List<Member>) GameServer.getInstance().getDataGameServerInterfaceSync().exec(MemberMapper.class,
//				MapperConstant.selectAll, null);
//		if (members != null) {
//			for (Member member : members) {
//				membersMap.put(member.getPlayerId(), member);
//				if (member.getUnionId() > 0) {
//					unionMembersMap.put(member.getUnionId(), member);
//				}
//			}
//		}
//
//		List<Union> unions = (List<Union>) GameServer.getInstance().getDataGameServerInterfaceSync().exec(UnionMapper.class,
//				MapperConstant.selectAll, null);
//
//		if (unions != null) {
//
//			for (Union union : unions) {
//				unionsMap.put(union.getId(), union);
//				unionsList.add(union);
//				unionNamesSet.add(union.getName());
//			}
//		}
//		List<UnionApplication> applications = (List<UnionApplication>) GameServer.getInstance().getDataGameServerInterfaceSync().exec(UnionApplicationMapper.class,
//				MapperConstant.selectAll, null);
//		
//		for (UnionApplication unionApplication : applications) {
//			unionApplicationsMap.put(unionApplication.getUnionId(), unionApplication.getPlayerId());
//		}

	}

	public Union getUnionByPlayerId(long playerId) {
		Member member = getMember(playerId);
		if (member == null) { 
			return null; 
		}
		return getUnion(member.getUnionId());
	}

	public Collection<Union> getUnions(int page) {

		int fromIndex = page * PAGE_SIZE;
		int toIndex = page * PAGE_SIZE + PAGE_SIZE;
		return getUnions(fromIndex, toIndex);
	}

	private List<Union> getUnions(int fromIndex, int toIndex) {

		if (fromIndex >= this.unionsList.size()) {
			return null;
		}
		if (toIndex > this.unionsList.size()) {
			toIndex = this.unionsList.size();
		}
		return this.unionsList.subList(fromIndex, toIndex);

	}

	public void applyUnion(long playerId, long unionId) {
		this.unionApplicationsMap.put(unionId, playerId);
	}

	public boolean addUnionApplication(long unionId, long playerId) {
		boolean remove = this.unionApplicationsMap.put(unionId, playerId);
		if (remove) {
			UnionApplication unionApplication = new UnionApplication();
			unionApplication.setPlayerId(playerId);
			unionApplication.setUnionId(unionId);
			DAO.execute(UnionApplicationMapper.class, MapperConstant.insert,
					unionApplication);
		}
		return remove;
	}

	public boolean delUnionApplication(long unionId, long playerId) {
		boolean remove = this.unionApplicationsMap.remove(unionId, playerId);
		if (remove) {
//			UnionApplication unionApplication = new UnionApplication();
//			unionApplication.setPlayerId(playerId);
//			unionApplication.setUnionId(unionId);
			DAO.execute(UnionApplicationMapper.class, MapperConstant.deleteByPrimaryKey,
					new Object[] { playerId, unionId });
		}
		return remove;

	}

	/**
	 * 某人加入到工会中
	 * @param playerId
	 * @param name
	 * @param union
	 * @return
	 */
	public boolean joinUnion(long playerId, long unionId) {
		Union union = getUnion(unionId); 
		if (union == null) {
			return false ; 
		}
		if (inUnion(playerId)) {
			return false ; 
		}
		Member member = getMember(playerId);
		if (member == null) {
			String name = "";
			Player player = PlayerManager.getInstance().getPlayer(playerId);
			if (player != null) {
				name = player.getData().getName();
			}
			member = initMember(playerId, name, UnionTitleEnum.Ordinary.v(), union.getId());
		} else {
			member.setTitle(UnionTitleEnum.Ordinary.v());
			member.setUnionId(union.getId());
			member.setJoinTime(System.currentTimeMillis());
			updateMember(member);
		}
		
		return true;

	}

	public boolean inUnion(long playerId) {
		Member member = getMember(playerId);
		return member != null && member.getUnionId() > 0;
	}
	/**
	 * 检查某人是否申请过某工会
	 * @param playerId
	 * @param unionId
	 * @return
	 */
	public boolean hasApplication(long playerId, long unionId) {
		Collection<Long> collection = this.unionApplicationsMap.get(unionId);
		return collection.contains(playerId);
	}

	/**
	 * 解散工会
	 * @param id
	 */
	public void disbandUnion(long id) {

		Collection<Member> collection = this.unionMembersMap.removeAll(id);
		for (Member member : collection) {
			resetMember(member);
		}
		Union u = this.unionsMap.remove(id);
		this.unionsList.remove(u);
		
		deleteUnion(id);
	}

	/**
	 * 移除工会中的某个成员
	 * @param playerId
	 * @param unionId
	 * @return
	 */
	public boolean removeMember(long playerId, long unionId) {
		Collection<Member> collection = this.unionMembersMap.get(unionId);
		Iterator<Member> iterator = collection.iterator();
		while (iterator.hasNext()) {
			Member member = iterator.next();
			if (member.getPlayerId() == playerId) {
				iterator.remove();
				break;
			}
		}
		Member member = getMember(playerId);
		if (member != null) {
			resetMember(member);
		}
		return true;
	}

	private void resetMember(Member member) {
		member.reset();
		updateMember(member);
	}

	public void deleteUnion(long id) {
		DAO.execute(UnionMapper.class, MapperConstant.deleteByPrimaryKey, id);
	}

	public boolean checkNameExist(String name) {
		return unionNamesSet.contains(name);
	}
	public Union createUnion(Player player, String name) {
		long playerId = player.getData().getPlayerId();
		Union union = new Union();
		union.setName(name);
		union.setLevel(1);
		union.setCreateTime(System.currentTimeMillis());
		union.setId(IdUtil.getIdAutoIncrease(IdType.UNION));

		this.unionsMap.put(union.getId(), union);
		unionsList.add(union);
		unionNamesSet.add(name);
		// TODO 初始化member

		Member member = getMember(playerId);
		if (member == null) {
			member = initMember(playerId, name, UnionTitleEnum.Leader.v(), union.getId());
		} else {
			member.setTitle(UnionTitleEnum.Leader.v());
			member.setUnionId(union.getId());
			member.setJoinTime(System.currentTimeMillis());
			updateMember(member);
		}
		return union;
	}

	public Member initMember(long playerId, String name, int title, long unionId) {

		Member member = new Member();
		member.setPlayerId(playerId);
		member.setTitle(title);
		member.setName(name);
		member.setUnionId(unionId);
		member.setJoinTime(System.currentTimeMillis());
		ObjUtil.setDefaultValue(member);
		insertMember(member);
		this.membersMap.put(playerId, member);
		if (unionId > 0) {
			this.unionMembersMap.put(unionId, member);
		}
		return member;
	}

	public void updateMember(Member member) {
		DAO.execute(MemberMapper.class, MapperConstant.updateByPrimaryKey, member);
	}
	public void insertMember(Member member) {
		DAO.execute(MemberMapper.class, MapperConstant.insert, member);
	}

	public void updateUnion(Union union) {
		DAO.execute(UnionMapper.class, MapperConstant.updateByPrimaryKey, union);
	}

}
