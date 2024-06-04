package cn.game.games.cache.op.impl;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import cn.game.games.cache.entity.BattlePass;
import cn.game.games.cache.op.face.IBattlePassOp;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.BattlePassMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.helper.ItemHelper;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.BattlePassConfig;
import cn.game.protocol.generated.config.BattlePassPrizeConfig;
import cn.game.protocol.generated.manager.BattlePassManager;
import cn.game.protocol.generated.manager.BattlePassPrizeManager;
import cn.game.protocol.protobuf.BattlePassMsg;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public class BattlePassOp extends BasePlayerModule implements IBattlePassOp {
    /**
     * 周期id
     */
    private int battlepassId;
    /**
     * 是否购买
     */
    private boolean recharge;
    /**
     * 等级
     */
    private int level;
    /**
     * 经验
     */
    private int exp;
    /**
     * 金色奖励
     */
    private BitSet goldRewardIndex;
    /**
     * 银色奖励
     */
    private BitSet silverRewardIndex;

    /**
     * 周期开启天数
     */
    private int openday;


    @Override
    public void initLoadData(BattlePass battlePass) {
        init();
        if (battlePass == null) {
            return;
        }
        this.battlepassId = battlePass.getBattlepassId();
        this.level = battlePass.getLevel();
        this.exp = battlePass.getExp();
        this.recharge = battlePass.getRecharge() == 1 ? true : false;
        this.openday = battlePass.getOpenday();
        this.silverRewardIndex = battlePass.getSilverRewardIndex();
        this.goldRewardIndex = battlePass.getGoldRewardIndex();
    }

    @Override
	public int getBattlepassId() {
        return battlepassId;
    }

    @Override
	public boolean isRecharge() {
        return recharge;
    }

    @Override
	public int getLevel() {
        return level;
    }

    @Override
	public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public BattlePass insertSelective(BattlePass battlePass) {
        DAO.execute(BattlePassMapper.class, MapperConstant.insertSelective, battlePass);
        return null;
    }

    @Override
    public BattlePass insert(BattlePass battlePass) {
        DAO.execute(BattlePassMapper.class, MapperConstant.insert, battlePass);
        return null;
    }

    @Override
    public void updateSelective(BattlePass battlePass) {
        DAO.execute(BattlePassMapper.class, MapperConstant.updateByPrimaryKeySelective, battlePass);
    }

    @Override
    public void update(BattlePass battlePass) {
        DAO.execute(BattlePassMapper.class, MapperConstant.updateByPrimaryKey, battlePass);
    }


    @Override
    public void updateBattlePassByIdAndOpenDay(int passid, int battlePassOpenDays) {
        BattlePass battlePass = new BattlePass();
        battlePass.setPlayerId(this.playerId);
        battlePass.setLevel(0);
        battlePass.setRecharge((byte) 0);
        battlePass.setBattlepassId(passid == 0 ? -1 : passid);
        battlePass.setExp(0);
        battlePass.setOpenday((byte) (passid == 0 ? 0 : battlePassOpenDays));
        battlePass.setGoldRewards("");
        battlePass.setSilverRewards("");
        update(battlePass);
    }

    @Override
    public void resetBattlePassData(int battlepassId, int battlePassOpenDays) {
        //过期状态不设置0 以防与初始状态混淆
        this.battlepassId = (battlepassId == 0 ? -1 : battlepassId);
        this.level = 0;
        this.recharge = false;
        this.exp = 0;
        this.openday = (battlepassId == 0 ? 0 : battlePassOpenDays);
        if (this.openday != 0) {
            for (int i = 1; i <= battlePassOpenDays; i++) {
                addBattlePassTask(this.battlepassId, i);
            }
        }
        this.goldRewardIndex = new BitSet();
        this.silverRewardIndex = new BitSet();
    }

    @Override
    public boolean buyBattlePass() {
        BattlePassConfig battlePassConfig = BattlePassManager.getInstance().getBattlePassConfig(battlepassId);
        if (battlePassConfig == null) {
            return false;
        }
        this.recharge = true;
        BattlePass pass = new BattlePass();
        pass.setPlayerId(playerId);
        pass.setRecharge((byte) 1);
        updateSelective(pass);
        
        return true;
    }

    @Override
	public List<RewardInfo> getBattlePassRewards() {
		List<RewardInfo> rewardItems = new ArrayList<>();
        for (int i = 1; i <= this.level; i++) {
            BattlePassPrizeConfig config = ItemHelper.getBattlePassConfig(this.battlepassId, i);
            if (config == null) {
                continue;
            }
            if (this.recharge && !this.goldRewardIndex.get(i)) {
                this.goldRewardIndex.set(i);
//                rewardItems.addAll(PlayerHelper.addResources(player, config.getGoldMedal()));
            }
            if (!this.silverRewardIndex.get(i)) {
                this.silverRewardIndex.set(i);
//                rewardItems.addAll(PlayerHelper.addResources(player, config.getSilverMedal()));
            }
        }
        updateBattlPassRewardsInfo();
        return rewardItems;
    }

    @Override
	public List<RewardInfo> getBattlePassLevelRewards(BattlePassMsg.MedalType type, int level) {
        BattlePassPrizeConfig config = ItemHelper.getBattlePassConfig(this.battlepassId, level);
        if (config == null) {
            return null;
        }
		List<RewardInfo> items = new ArrayList<>();
        switch (type) {
            case GOLD:
                if (this.goldRewardIndex.get(level)) {
                    return null;
                }
//                items.addAll(PlayerHelper.addResources(player, config.getGoldMedal()));
                this.goldRewardIndex.set(level);
                break;
            case SILVER:
                if (this.silverRewardIndex.get(level)) {
                    return null;
                }
//                items.addAll(PlayerHelper.addResources(player, config.getSilverMedal()));
                this.silverRewardIndex.set(level);
                break;
            default:
                break;
        }
        updateBattlPassRewardsInfo();

        return items;
    }

    @Override
    public void updateBattlPassRewardsInfo() {
        BattlePass pass = new BattlePass();
        pass.setPlayerId(playerId);
        pass.setGoldRewards(JSONObject.toJSONString(this.goldRewardIndex.toLongArray()));
        pass.setSilverRewards(JSONObject.toJSONString(this.silverRewardIndex.toLongArray()));
        updateSelective(pass);
    }

    @Override
    public void buildBattlePassInfoResp(BattlePassMsg.BattlePassInfoResponse_06000002.Builder resp) {
        resp.setId(this.battlepassId);
        resp.setExp(this.exp);
        resp.setLevel(this.level);
        resp.setRecharge(this.recharge);
        resp.setOpenday(this.openday);

        for (int i = this.goldRewardIndex.nextSetBit(0); i >= 0; i = this.goldRewardIndex.nextSetBit(i + 1)) {
            resp.addGoldLevels(i);
        }
        for (int i = this.silverRewardIndex.nextSetBit(0); i >= 0; i = this.silverRewardIndex.nextSetBit(i + 1)) {
            resp.addSilverLevels(i);
        }
    }


    public void addBattlePassTask(int battlepassId, int openday) {
        BattlePassConfig battlePassConfig = BattlePassManager.getInstance().getBattlePassConfig(battlepassId);
        if (battlePassConfig == null) {
            throw new IllegalArgumentException("battlepass id not exist, battlepassid :" + battlepassId);
        }
        try {
            List<List<Integer>> taskId = battlePassConfig.getTaskId();
            List<Integer> integers = taskId.get(openday - 1);
            log.info("playerid:{}, battlepassid:{}, battlepass周期开启 第{} 天", playerId, battlepassId, openday);
            QuestModule questOp = player.getModule(QuestModule.class);
            integers.forEach(e -> questOp.open(e, true));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void addExp(int exp) {
        BattlePassPrizeConfig config = ItemHelper.getBattlePassConfig(this.battlepassId, this.level);
        if(config == null) {
            return;
        }
        BattlePassPrizeConfig nextconfig = ItemHelper.getBattlePassConfig(this.battlepassId, this.level + 1);
        this.exp += exp;
        //消耗的经验值
        while (this.exp >= config.getUnlockCost() && nextconfig != null) {
            this.exp -= config.getUnlockCost();
            this.level += 1;
            config = ItemHelper.getBattlePassConfig(this.battlepassId, this.level);
            nextconfig = ItemHelper.getBattlePassConfig(this.battlepassId, this.level + 1);
        }
        if (this.exp > config.getUnlockCost()) {
            this.exp = config.getUnlockCost();
        }
        BattlePass battlePass = new BattlePass();
        battlePass.setPlayerId(playerId);
        battlePass.setLevel(this.level);
        battlePass.setExp(this.exp);
        updateSelective(battlePass);
    }

    /**
     * 初始化
     */
    public void initBattlePassCycle() {
        int passid = ItemHelper.getBattlePassId();
        //battlepass 已开启
        if (passid == this.battlepassId) {
            return;
        }
        //新增周期
        if (passid != 0 && this.battlepassId == 0) {
            this.battlepassId = passid;
            //当前日期与开启日期相差多少天
            int battlePassOpenDays = ItemHelper.getBattlePassOpenDays(passid);
            for (int i = 1; i <= battlePassOpenDays; i++) {
                this.openday = i;
                addBattlePassTask(this.battlepassId, i);
            }
            //入库
            BattlePass insert = new BattlePass();
            insert.setPlayerId(playerId);
            insert.setBattlepassId(this.battlepassId);
            insert.setOpenday((byte) this.openday);
            insertSelective(insert);
            return;
        }
        //更新周期
        if (passid != 0 && this.battlepassId == -1) {
            int battlePassOpenDays = ItemHelper.getBattlePassOpenDays(passid);
            resetBattlePassData(passid, battlePassOpenDays);
            //更新库
            updateBattlePassByIdAndOpenDay(passid, battlePassOpenDays);
        }
    }

    @Override
    public void dealwithBattlePassCycle() {
        int passid = ItemHelper.getBattlePassId();
        //battlepass 未开启
        if (passid == 0 && this.battlepassId == 0) {
            return;
        }
        //已经重置过期状态
        if (passid == 0 && this.battlepassId == -1) {
            return;
        }
        int battlePassOpenDays = ItemHelper.getBattlePassOpenDays(passid);
        //周期未改变 增加天数
        if (this.battlepassId == passid) {
            boolean ret = false;
            for (int i = this.openday + 1; i <= battlePassOpenDays; i++) {
                ret = true;
                this.openday++;
                addBattlePassTask(this.battlepassId, this.openday);
            }
            if (ret) {
                BattlePass battlePass = new BattlePass();
                battlePass.setPlayerId(playerId);
                battlePass.setOpenday((byte) this.openday);
                updateSelective(battlePass);
            }
            return;
        }
        //周期改变了 需要将上一周期没领的等级奖励通过邮件发给玩家

        for (int i = 1; i <= this.level; i++) {
            if (!this.goldRewardIndex.get(i)) {
                int prizeid = this.battlepassId * 100 + i;
                BattlePassPrizeConfig config = BattlePassPrizeManager.getInstance().getBattlePassPrizeConfigNullable(prizeid);
                if (config != null) {
                    List<Map.Entry<Integer, Integer>> goldMedal = config.getGoldMedal();
					MailHelper.sendMailMultiLanguage(playerId, 0, 208011, 208009, 208010, MailHelper.SYSTEM, goldMedal);
                }
            }
            if (!this.silverRewardIndex.get(i)) {
                int prizeid = this.battlepassId * 100 + i;
                BattlePassPrizeConfig config = BattlePassPrizeManager.getInstance().getBattlePassPrizeConfigNullable(prizeid);
                if (config != null) {
                    List<Map.Entry<Integer, Integer>> silverMedal = config.getSilverMedal();
					MailHelper.sendMailMultiLanguage(playerId, 0, 208011, 208009, 208010, MailHelper.SYSTEM, silverMedal);
                }
            }
        }
        resetBattlePassData(passid, battlePassOpenDays);
        //更新库
        updateBattlePassByIdAndOpenDay(passid, battlePassOpenDays);
    }

	@Override
	public EventTypeEnum[] getEventTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleEvent(GameEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public Class<?>[] defaultDbMapperClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void initFromDb(ListIterator<?> iterator) {
		// TODO Auto-generated method stub

	}
	@Override
	public void initFromDbAfter() {

	};

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public BitSet getGoldRewardIndex() {
		return goldRewardIndex;
	}

	public void setGoldRewardIndex(BitSet goldRewardIndex) {
		this.goldRewardIndex = goldRewardIndex;
	}

	public BitSet getSilverRewardIndex() {
		return silverRewardIndex;
	}

	public void setSilverRewardIndex(BitSet silverRewardIndex) {
		this.silverRewardIndex = silverRewardIndex;
	}

	public int getOpenday() {
		return openday;
	}

	public void setOpenday(int openday) {
		this.openday = openday;
	}

	public void setBattlepassId(int battlepassId) {
		this.battlepassId = battlepassId;
	}

	public void setRecharge(boolean recharge) {
		this.recharge = recharge;
	}
	

}
