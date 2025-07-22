package cn.game.games.net.game.module.guarantee;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import cn.game.protocol.generated.config.GuaranteeConfig;
import cn.game.protocol.generated.manager.GuaranteeManager;

public class Guarantee {

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
		Set<Integer> allRounds = GuaranteeManager.instance().getTypeRounds().keySet();
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
			nexGuaranteeConfig = typeRoundList.get(0);
			this.stage = 1;
			this.round++;
		} else {
			this.stage++;
		}
		this.id = nexGuaranteeConfig.ID;
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

}
