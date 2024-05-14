package cn.game.games.cache.op.impl;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import cn.game.games.cache.entity.DayOperation;
import cn.game.games.cache.op.face.IDayOp;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.DayOperationMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.util.DAO;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

/**    
 * 玩家每日的各种操作计数
 * @date 2024年1月25日 下午3:38:21
 * @author SYQ
 */
public class DayOp extends BasePlayerModule implements IDayOp{
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.NewDay };

	private Map<Integer, DayOperation> dayOperations  ; 
	
	@Override
	public void init() {
		dayOperations = new HashMap<Integer, DayOperation>() ; 
	}
	
	@Override
	public void initLoadData(List<DayOperation> dayOperations) {
		if (dayOperations==null) {
			return ; 
		}
		for (DayOperation dayOperation : dayOperations) {
			this.dayOperations.put(dayOperation.getType(), dayOperation) ; 
		}
		
	}

	@Override
	public int getCount(OpType type) {
		
		DayOperation dayOperation = this.dayOperations.get(type.getId());
		if (dayOperation==null) {
			return 0 ; 
		}
		return dayOperation.getCount();
	}

	@Override
	public void addCount(OpType type) {
		addCount(type, 1) ; 
	}

	@Override
	public void addCount(OpType type, int count) {
		DayOperation dayOperation = this.dayOperations.get(type.getId());
		if (dayOperation==null) {
			dayOperation = new DayOperation() ; 
			dayOperation.setPlayerId(playerId) ; 
			dayOperation.setType(type.getId());
			dayOperation.setCount(count) ; 
			insert(dayOperation) ; 
			this.dayOperations.put(type.getId(), dayOperation);
		} else {
			dayOperation.setCount(dayOperation.getCount()+count) ; 
			update(dayOperation) ; 
		}
		
	}
	@Override
	public void insert(DayOperation dayOperation) {
		DAO.execute(DayOperationMapper.class, MapperConstant.insert, dayOperation) ; 
	}

	@Override
	public void update(DayOperation dayOperation) {
		DAO.execute(DayOperationMapper.class, MapperConstant.updateByPrimaryKeySelective, dayOperation) ; 
	}

	@Override
	public boolean reset() {
		
		DAO.execute(DayOperationMapper.class, "updateByPlayerId", playerId);
		for (DayOperation operation : this.dayOperations.values()) {
			operation.setCount(0) ; 
		}
		return true;
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {
		case NewDay: {
//			reset();
			break;
		}
		}
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

}
