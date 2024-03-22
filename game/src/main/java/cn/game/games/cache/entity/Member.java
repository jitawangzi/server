package cn.game.games.cache.entity;

import java.io.Serializable;

import cn.game.util.ObjUtil;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class Member implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 玩家名字，在这里记录一份方便显示
	 * @mbg.generated
	 */
	private String name;
	/**
	 * 工会成员职位
	 * @mbg.generated
	 */
	private Integer title;
	/**
	 * 贡献值
	 * @mbg.generated
	 */
	private Long contribution;
	/**
	 * 加入工会时间
	 * @mbg.generated
	 */
	private Long joinTime;
	/**
	 * 退出工会时间
	 * @mbg.generated
	 */
	private Long quitTime;
	/**
	 * 所在工会id
	 * @mbg.generated
	 */
	private Long unionId;
	/**
	 * @mbg.generated
	 */
	private Long contributionToday;
	/**
	 * @mbg.generated
	 */
	private Long contributionWeek;
	/**
	 * @mbg.generated
	 */
	private Long offLineTime;
	/**
	 * 捐献记录
	 * @mbg.generated
	 */
	private String contributionRecords;
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
	public String getName() {
		return name;
	}

	/**
	 * @mbg.generated
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getTitle() {
		return title;
	}

	/**
	 * @mbg.generated
	 */
	public void setTitle(Integer title) {
		this.title = title;
	}

	/**
	 * @mbg.generated
	 */
	public Long getContribution() {
		return contribution;
	}

	/**
	 * @mbg.generated
	 */
	public void setContribution(Long contribution) {
		this.contribution = contribution;
	}

	/**
	 * @mbg.generated
	 */
	public Long getJoinTime() {
		return joinTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setJoinTime(Long joinTime) {
		this.joinTime = joinTime;
	}

	/**
	 * @mbg.generated
	 */
	public Long getQuitTime() {
		return quitTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setQuitTime(Long quitTime) {
		this.quitTime = quitTime;
	}

	/**
	 * @mbg.generated
	 */
	public Long getUnionId() {
		return unionId;
	}

	/**
	 * @mbg.generated
	 */
	public void setUnionId(Long unionId) {
		this.unionId = unionId;
	}

	/**
	 * @mbg.generated
	 */
	public Long getContributionToday() {
		return contributionToday;
	}

	/**
	 * @mbg.generated
	 */
	public void setContributionToday(Long contributionToday) {
		this.contributionToday = contributionToday;
	}

	/**
	 * @mbg.generated
	 */
	public Long getContributionWeek() {
		return contributionWeek;
	}

	/**
	 * @mbg.generated
	 */
	public void setContributionWeek(Long contributionWeek) {
		this.contributionWeek = contributionWeek;
	}

	/**
	 * @mbg.generated
	 */
	public Long getOffLineTime() {
		return offLineTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setOffLineTime(Long offLineTime) {
		this.offLineTime = offLineTime;
	}

	/**
	 * @mbg.generated
	 */
	public String getContributionRecords() {
		return contributionRecords;
	}

	/**
	 * @mbg.generated
	 */
	public void setContributionRecords(String contributionRecords) {
		this.contributionRecords = contributionRecords;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.MemberMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return playerId;
	}

	public static Member valueOf(long playerId, long unionId, int title) {
		Member member = new Member();
		member.playerId = playerId;
		member.unionId = unionId;
		member.title = title;
		member.joinTime = System.currentTimeMillis();
		ObjUtil.setDefaultValue(member);
		return member;
	}

	public void reset() {

		this.setQuitTime(System.currentTimeMillis());
		this.setUnionId(0L);
		this.setTitle(0);
//		this.getContributionRecordMap().clear();
		this.contribution = 0L;
		this.contributionToday = 0L;
		this.contributionWeek = 0L;

	}
}