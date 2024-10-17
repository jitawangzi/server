package cn.game.games.net.game.module.develop.attr;

import java.util.Collection;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.module.develop.fairyfriend.FairyFriend;
import cn.game.games.net.game.module.develop.fairyfriend.FairyFriendModule;
import cn.game.protocol.generated.config.FairyFriendFavorabilityConfig;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.FairyFriendFavorabilityManager;

/**    
 * 仙友加成
 * 2024年10月17日 17:14:00
 * @author SYQ
 */
public class FairyFriendAttrCalc extends PlayerAttrCalc {

	public FairyFriendAttrCalc(Player player) {
		super(player);
	}

	@Override
	public void calcAttr() {
		FairyFriendModule fairyFriendModule = player.getModule(FairyFriendModule.class);
		Collection<FairyFriend> list = fairyFriendModule.list();
		for (FairyFriend fairyFriend : list) {
			FairyFriendFavorabilityConfig favorabilityConfig = FairyFriendFavorabilityManager
					.instance()
					.getUIFairyListIDLV(fairyFriend.getConfigId(), fairyFriend.getLevel());
			if (favorabilityConfig == null) {
				continue;
			}
			for (int[] attrs : favorabilityConfig.Attr) {
				attrMap.add(attrs[0], attrs[1]);
			}
		}
	}
	@Override
	public InitialUI getFunction() {
		return InitialUI.FairyFriends;
	}

	@Override
	public AttrCalcType getAttrCalcType() {
		return AttrCalcType.FairyFriend;
	}
}
