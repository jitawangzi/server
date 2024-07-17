package cn.game.core.map;

import java.io.Serializable;

/**
 * 实际是地图格子的坐标
 * 2020年12月24日 下午5:55:16
 * @author SYQ
 */
public class Grid implements Serializable {

	/**  */
	private static final long serialVersionUID = -130873468566184913L;
	public int x;
	public int y;

	public Grid(int x, int y) {
		this.x = x;
		this.y = y;
	}
	public Grid() {
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!getClass().isAssignableFrom(obj.getClass()) && !obj.getClass().isAssignableFrom(getClass())) {
			return false;
		}
		Grid other = (Grid) obj;
		if (getX() != other.getX())
			return false;
		if (getY() != other.getY())
			return false;
		return true;
	}
	@Override
	public String toString() {
		return this.x + "," + this.y;
	}


}
