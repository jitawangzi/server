package cn.game.games.net.game.helper;

import java.util.List;

import cn.game.protocol.generated.config.RoleConfig;
import cn.game.protocol.generated.enume.AttributeSubTypeEnum;
import cn.game.protocol.generated.enume.AttributeTypeEnum;
import cn.game.protocol.generated.manager.RoleManager;

public class RoleHelper {
	
	/**
	 * 获取历程标签下的各分类最大数量
	 * 
	 * @param type2
	 * @return
	 */
	public static int getRoleTagCount(int type2) {
		if (type2 == 3 || type2 == 4 || type2 == 5) {
			return 2; 
		}
		if (type2 == 6 || type2 == 7) {
			return 1 ; 
		}
		return 0 ; 
	}
	
	/**
	 * 获取某角色礼物卡的栏位
	 * @param roleConfigId
	 * @param giftCardId
	 * @return
	 */
	public static int getGiftCardLocation(int roleConfigId, int giftCardId) {
		RoleConfig roleConfigNullable = RoleManager.getInstance().getRoleConfigNullable(roleConfigId);
		List<List<Integer>> giftCards = roleConfigNullable.getGiftCards();
		for (int i = 0; i < giftCards.size(); i++) {
			//礼物卡:角色id:好感度
			List<Integer> list = giftCards.get(i);
			if (list.get(0) == giftCardId) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * 根据角色属性类型,获取属性大类型
	 * @param roleAttribute
	 * @return
	 */
	public static AttributeTypeEnum getAttributeType(int roleAttribute) {
		int id = roleAttribute / 100;
		return AttributeTypeEnum.get(id);
	}
	
	/**
	 * 根据角色属性类型,获取属性子类型
	 * @param roleAttribute
	 * @return
	 */
	public static AttributeSubTypeEnum getSubType(int roleAttribute) {
		int id = roleAttribute % 100;
		return AttributeSubTypeEnum.get(id);
	}
	
	/**
	 * 根据大属性枚举和子属性枚举获取RoleAttributeId
	 * @param type
	 * @param subType
	 * @return
	 */
	public static int makeRoleAttributeId(AttributeTypeEnum type, AttributeSubTypeEnum subType) {
		int high = type.getId();
		int low = subType.getId();
		return high * 100 + low;
	}

}
