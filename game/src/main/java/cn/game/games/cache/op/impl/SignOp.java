package cn.game.games.cache.op.impl;

import java.util.ListIterator;

import cn.game.games.cache.entity.Sign;
import cn.game.games.cache.op.face.ISignOp;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.SignMapper;
import cn.game.games.util.DAO;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.util.DateUtil;

public class SignOp extends BasePlayerModule implements ISignOp {
	private Sign sign;
	private static final char YES = '1';
	private static final char NO = '0';

	@Override
	public void init() {
		this.sign = new Sign();
	}

	@Override
	public void initLoadData(Sign sign) {
		if (sign != null) {
			this.sign = sign;
			int nowTime = getYearAndMonth();
			int time = sign.getTime();
			if (time != nowTime) {
				refresh();
			}
			return;
		}
		refresh();
	}

	@Override
	public String seeSign() {
		return sign.getSignInfo();
	}

	@Override
	public boolean sign() {
		int nowDay = DateUtil.getDayOfMonth();
		char info = sign.getSignInfo().charAt(nowDay - 1);
		if (info == YES) {
			return false;
		}

		char[] array = sign.getSignInfo().toCharArray();
		array[nowDay - 1] = YES;
		String infos = String.valueOf(array);
		sign.setSignInfo(infos);
		sign.setPlayerId(playerId);

		int nowTime = getYearAndMonth();
		if (sign.getTime() == null) {
			sign.setTime(nowTime);
			insert();
		} else {
			sign.setTime(nowTime);
			update();
		}
		return true;
	}

	@Override
	public boolean refresh() {
		int days = DateUtil.getNowMonthHowDays();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < days; i++) {
			sb.append(NO);
		}
		this.sign.setSignInfo(sb.toString());
		return true;
	}

	@Override
	public int getRewardConfId() {
		int month = DateUtil.getMonth() + 1;
		int day = DateUtil.getDayOfMonth();
		return month * 100 + day;
	}

	@Override
	public void insert() {
		DAO.insert(SignMapper.class, this.sign);
	}

	@Override
	public void update() {
		DAO.update(SignMapper.class, this.sign);
	}

	/**
	 * 拼接年份和月份
	 * 
	 * @return
	 */
	private int getYearAndMonth() {
		int month = DateUtil.getMonth() + 1;
		int year = DateUtil.getYear();
		// 如202012
		return year * 100 + month;
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
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}

}
