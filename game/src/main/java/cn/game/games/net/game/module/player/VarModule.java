package cn.game.games.net.game.module.player;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import cn.game.games.cache.entity.Variable;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.VariableMapper;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

/**    
 * 专门处理玩家的一些零散的int和boolean类型变量。 并且这种变量大多修改的不频繁
 * 修改频繁的变量可以放到t_player_data表中。
 * @date 2024年2月26日 下午6:19:36
 * @author SYQ
 */
public class VarModule extends BasePlayerModule {
	private static transient EventTypeEnum[] events = new EventTypeEnum[] {};

	private Map<Integer, Integer> varMap = new HashMap<Integer, Integer>();

	public void setVar(int type, int value) {

		Integer variable = varMap.get(type);
		if (variable == null) {
			varMap.put(type, variable);
			Variable newVar = new Variable();
			newVar.setPlayerId(playerId);
			newVar.setType(type);
			newVar.setValue(value);
			newVar.insert();
		} else {
			varMap.put(type, value);
			Variable newVar = new Variable();
			newVar.setPlayerId(playerId);
			newVar.setType(type);
			newVar.setValue(value);
			newVar.update();
		}
	}
	public void setVar(int type, boolean value) {
		setVar(type, value == true ? 1 : 0);
	}

	public int getVar(int type) {
		Integer variable = varMap.get(type);
		return variable == null ? 0 : variable;
	}

	public boolean getBoolVar(int type) {
		Integer variable = varMap.get(type);
		return variable != null && variable > 0;
	}

	public void clearVar(int type) {
		Integer remove = varMap.remove(type);
		if (remove != null) {
			Variable v = new Variable();
			v.setPlayerId(playerId);
			v.setType(type);
			v.delete();
		}
	}

	public int incrVar(int type) {
		int var = getVar(type);
		int newVar = var + 1;
		setVar(type, newVar);
		return newVar;
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
//		builder.setPlayer(player.toProto());
	}

	@Override
	public Class<?>[] defaultDbMapperClass() {
		return new Class[] { VariableMapper.class };
	}

	@Override
	protected void initFromDb(ListIterator<?> iterator) {
		List<Variable> list = (List<Variable>) iterator.next();
		for (Variable variable : list) {
			varMap.put(variable.getType(), variable.getValue());
		}
	}
	@Override
	public void initFromDbAfter() {

	};
	@Override
	public void handleEvent(GameEvent event) {

	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
}
