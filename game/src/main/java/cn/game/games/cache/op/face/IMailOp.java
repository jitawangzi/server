package cn.game.games.cache.op.face;

import java.util.Collection;
import java.util.List;

import cn.game.games.cache.entity.Mail;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public interface IMailOp{
	
	//初始数据,
	public int initLoadData(List<Mail> mails);
	
	public void sendOnline(Mail mail);
	
	public Mail get(long id);
	
	public void update(Mail mail) ; 

	public void updateSelective(Mail mail);
	
	public void delete(long id) ; 
	
	public Collection<Mail> list() ; 
	
	public boolean hasNoRead() ; 
	
	public void see(long id);
	
	public void seeBatch();

	public List<RewardInfo> receive(long id);

	public List<RewardInfo> receiveBatch();

}
