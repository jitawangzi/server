package cn.game.games.cache.op.face;

import java.util.List;

import cn.game.games.cache.entity.BattlePass;
import cn.game.protocol.protobuf.BattlePassMsg;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public interface IBattlePassOp {

    public void initLoadData(BattlePass battlePass);

    public BattlePass insertSelective(BattlePass battlePass);

    public BattlePass insert(BattlePass battlePass);

    public void updateSelective(BattlePass battlePass);

    public void update(BattlePass battlePass);

    /**
     * 周期检查
     */
    public void dealwithBattlePassCycle();

    public void updateBattlePassByIdAndOpenDay(int passid, int battlePassOpenDays);

    /**
     * 重置数据
     * @param battlepassId
     */
    public void resetBattlePassData(int battlepassId, int battlePassOpenDays);

    /**
     * 购买battlepass
     * @return
     */
    public boolean buyBattlePass();

    /**
     * 一键领取奖励
     */
	public List<RewardInfo> getBattlePassRewards();

	public List<RewardInfo> getBattlePassLevelRewards(BattlePassMsg.MedalType type, int level);

    public void updateBattlPassRewardsInfo();

    public void buildBattlePassInfoResp(BattlePassMsg.BattlePassInfoResponse_06000002.Builder resp);

    public int getBattlepassId();

    public boolean isRecharge();

    public int getLevel();

    public void setLevel(int level);

    /**
     * 添加经验
     * @param exp
     */
    public void addExp(int exp);

}
