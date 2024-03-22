package cn.game.games.cache.op.face;

import cn.game.games.core.event.EventHandler;
import cn.game.games.net.game.module.prop.RolePropFromType;
import cn.game.protocol.generated.enume.AttributeTypeEnum;

public interface IPropertyOp extends EventHandler {

	/**
	 * 计算属性
	 * @param id  roleId或ExploreRole的uid
	 */
	public void updateProperty(int id);

	/**
	 * get属性max值
	 * 
	 * @param id roleId或ExploreRole的uid
	 * @param type
	 * @return
	 */
	int getProperty(int id, AttributeTypeEnum type);

	/**
	 * 得到角色属性模块中不同类型的值
	 * @param id roleId或ExploreRole的uid
	 * @param type
	 * @param attribteId
	 * @return
	 */
	int getRolePropTypeEnumValue(int id, RolePropFromType type, int attribteId);

	/**
	 * 面板属性
	 * @param roleId
	 * @param attribteId
	 * @return
	 */
	public int getPanelValue(int roleId, int attribteId);

	/**
	 * 初始化某角色的属性
	 * @param id
	 */
	public void initPropertyById(int id);

}
