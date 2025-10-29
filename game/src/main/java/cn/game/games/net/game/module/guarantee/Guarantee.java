package cn.game.games.net.game.module.guarantee;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import cn.game.protocol.generated.config.GuaranteeConfig;
import cn.game.protocol.generated.manager.GuaranteeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Guarantee {

	private static final Logger log = LoggerFactory.getLogger(Guarantee.class);
	/** GuaranteeConfig 表ID */
	private int id;
	/** 当前轮次 */
	private int round = 1;
	/** 当前阶段*/
	private int stage = 1;
	/** 当前阶段已经累计了多少次 */
	private int count;

	/** 
	 * 是否在保底状态
	 * @return
	 */
	public boolean isInGuarantee() {
		GuaranteeConfig guaranteeConfig = GuaranteeManager.instance().get(id);
		return guaranteeConfig.isMaxReset == false && guaranteeConfig.count <= count;

	}
	/** 
	 * 
	 * @param count
	 * @return 如果触发了保底，返回触发保底时的配置表id
	 */
	public int addCount(int count) {
		int ret = 0;
		for (int i = 0; i < count; i++) {
			this.count++;
			log.info("抽卡日志 幸运值+1 id:{} round:{} stage:{} count:{}", id, round, stage, this.count);
			GuaranteeConfig guaranteeConfig = GuaranteeManager.instance().get(id);
			if (this.count >= guaranteeConfig.count) {// 触发保底
				ret = this.id;
				if (guaranteeConfig.isMaxReset) {
					reset();
				}
			}
		}
		return ret;
	}

	/** 
	 * 重置保底，可能进入到下一阶段。 
	 * @param guaranteeConfig
	 */
	public void reset() {
		GuaranteeConfig guaranteeConfig = GuaranteeManager.instance().get(id);
		this.count = this.count - guaranteeConfig.count; 
		if (this.count < 0) {
			this.count = 0 ; 
		}
		//  返回的是type 的set
		Set<Integer> allRounds = GuaranteeManager.instance().getTypeRoundMap(guaranteeConfig.type).keySet();
		int max = Collections.max(allRounds);
		int roundToQuary = this.round > max ? max : this.round;

		List<GuaranteeConfig> typeRoundList = GuaranteeManager.instance().getTypeRoundList(guaranteeConfig.type, roundToQuary);
		if (typeRoundList == null) {
			return;
		}
		// 还有没有下一个阶段
		boolean hasNextStage = false;
		GuaranteeConfig nexGuaranteeConfig = null;
		for (GuaranteeConfig guaranteeConfig2 : typeRoundList) {
			if (guaranteeConfig2.stage == stage + 1) {
				hasNextStage = true;
				nexGuaranteeConfig = guaranteeConfig2;
				break;
			}
		}
		if (!hasNextStage) {
			this.stage = 1;
			this.round++;
			roundToQuary = this.round > max ? max : this.round;
			typeRoundList = GuaranteeManager.instance().getTypeRoundList(guaranteeConfig.type, roundToQuary);
			nexGuaranteeConfig = typeRoundList.get(0);
		} else {
			this.stage++;
		}
		this.id = nexGuaranteeConfig.ID;
		log.info("触发幸运值充值 id:{} round:{} stage:{} count:{}", id, round, stage, this.count);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public int getStage() {
		return stage;
	}

	public void setStage(int stage) {
		this.stage = stage;
	}

	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
}
