package cn.game.games.cache.entity;

public class ItemNoStack extends Item {

	protected boolean isStack = false;

	/** 等级 */
	protected int level;
	/** 星级 */
	protected int star;

	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @mbg.generated
	 */
	public Boolean getIsStack() {
		return isStack;
	}

	/**
	 * @mbg.generated
	 */
	public void setIsStack(Boolean isStack) {
		this.isStack = isStack;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getStar() {
		return star;
	}

	public void setStar(int star) {
		this.star = star;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.ItemNoStackMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return isStack;
	}

}