package cn.game.core.manager;

import java.util.Arrays;

/**
 * 不可变的标签路径类
 * <p>
 * 封装标签路径，提供更好的比较和哈希功能，解决String[]作为Map键的问题。
 * </p>
 */
public final class TagPath {
	private final String[] path;
	private final String stringRepresentation;

	/**
	 * 创建标签路径
	 *
	 * @param tags 路径标签数组
	 */
	public TagPath(String... tags) {
		this.path = tags != null ? tags.clone() : new String[0];
		this.stringRepresentation = String.join("/", this.path);
	}

	/**
	 * 获取路径标签数组（返回副本以保证不可变性）
	 *
	 * @return 路径标签数组
	 */
	public String[] getPath() {
		return path.clone();
	}

	/**
	 * 获取路径长度
	 *
	 * @return 路径中的标签数量
	 */
	public int length() {
		return path.length;
	}

	/**
	 * 获取指定索引位置的标签
	 *
	 * @param index 索引位置
	 * @return 标签
	 * @throws IndexOutOfBoundsException 如果索引越界
	 */
	public String get(int index) {
		return path[index];
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof TagPath))
			return false;
		TagPath that = (TagPath) o;
		return Arrays.equals(path, that.path);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(path);
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}

	/**
	 * 从字符串路径创建TagPath对象
	 *
	 * @param pathString 以"/"分隔的路径字符串
	 * @return 新的TagPath对象
	 */
	public static TagPath fromString(String pathString) {
		if (pathString == null || pathString.isEmpty()) {
			return new TagPath();
		}
		return new TagPath(pathString.split("/"));
	}
}