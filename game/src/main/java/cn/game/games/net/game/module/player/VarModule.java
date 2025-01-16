package cn.game.games.net.game.module.player;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.protocol.protobuf.PlayerMsg;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.util.IntMapWrapper;
import org.apache.commons.validator.Var;

/**    
 * 专门处理玩家的一些零散的int和boolean类型变量。 
 * 各种次数等等，都可以放这里
 * key 放到 @VarConstant
 * 2024年2月26日 下午6:19:36
 * @author SYQ
 */
public class VarModule extends BasePlayerModule {
	private static transient EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE };

	private IntMapWrapper varMap = new IntMapWrapper();

	public void setVar(int type, int value) {

//		Integer variable = varMap.get(type);
//		if (variable == null) {
//			varMap.put(type, variable);
//			Variable newVar = new Variable();
//			newVar.setPlayerId(playerId);
//			newVar.setType(type);
//			newVar.setValue(value);
//			newVar.insert();
//		} else {
//			varMap.put(type, value);
//			Variable newVar = new Variable();
//			newVar.setPlayerId(playerId);
//			newVar.setType(type);
//			newVar.setValue(value);
//			newVar.update();
//		}
		varMap.setValue(type, value);

	}
	public void setVar(int type, boolean value) {
		setVar(type, value == true ? 1 : 0);
	}

	public int getVar(int type) {
		return varMap.getValue(type);
	}

	public int addVar(int type,int value) {
		return varMap.add(type, value);
	}

	public int addVar(int type) {
		return varMap.add(type, 1);
	}

	public boolean getBoolVar(int type) {
		int variable = getVar(type);
		return  variable > 0;
	}

	public void clearVar(int type) {
		Integer remove = varMap.remove(type);
//		if (remove != null) {
//			Variable v = new Variable();
//			v.setPlayerId(playerId);
//			v.setType(type);
//			v.delete();
//		}
	}

	public int incrVar(int type) {
//		int var = getVar(type);
//		int newVar = var + 1;
//		setVar(type, newVar);
//		return newVar;
		return varMap.add(type, 1);
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		builder.setRenameCount(getVar(VarConstant.RANAME_COUNT));

		//微信设置
		PlayerMsg.WechatSetting.Builder setting = PlayerMsg.WechatSetting.newBuilder();
		setting.setIsOpenNotifyEnergy(getBoolVar(VarConstant.WECHAT_NOTIFY_ENERGY));
		setting.setIsOpenNotifyAoYouReward(getBoolVar(VarConstant.WECHAT_NOTIFY_AOYOU_REWARD));
		setting.setIsOpenNotifyFirstRechargeReward(getBoolVar(VarConstant.WECHAT_NOTIFY_FIRST_RECHARGE_REWARD));
		setting.setIsOpenNotifyMonthSignReward(getBoolVar(VarConstant.WECHAT_NOTIFY_MONTH_SIGN_REWARD));
		builder.setWechatSetting(setting);
	}

	@Override
	public Class<?>[] defaultDbMapperClass() {
//		return new Class[] { VariableMapper.class };
		return null;
	}

//	@Override
//	protected void initFromDb(ListIterator<?> iterator) {
//		List<Variable> list = (List<Variable>) iterator.next();
//		for (Variable variable : list) {
//			varMap.put(variable.getType(), variable.getValue());
//		}
//	}
	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {
		case PLAYER_CREATE: {
			incrVar(VarConstant.WALL_LEVEL);
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + event);
		}
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
}
