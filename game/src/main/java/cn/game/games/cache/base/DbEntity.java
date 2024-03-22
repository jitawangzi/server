package cn.game.games.cache.base;

import cn.game.games.util.DAO;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Future;

public interface DbEntity {

	public default Future<@Nullable Object> insert() {
		Class<?> mapperClass = getMapperClass();
		if (mapperClass == null) {
			return Future.succeededFuture();
		}
		this.beforeSave();
		return DAO.insert(mapperClass, this);
	}

	public default Future<@Nullable Object> insertOrUpdate() {
		Class<?> mapperClass = getMapperClass();
		if (mapperClass == null) {
			return Future.succeededFuture();
		}
		this.beforeSave();
		return DAO.insertOrUpdate(mapperClass, this);
	}

	/** 
	 * 只更新基本类型的更新方法。 如果确定只修改了基本类型的数据，建议调用这个方法
	 * @return
	 */
	public default Future<@Nullable Object> update() {
		Class<?> mapperClass = getMapperClass();
		if (mapperClass == null) {
			return Future.succeededFuture();
		}
		this.beforeSave();
		return DAO.update(mapperClass, this);
	}

	/** 
	 * 可以更新blob的更新方法，没有blob字段的也可以调用。 
	 * @return
	 */
	public default Future<@Nullable Object> updateWithBlobs() {
		Class<?> mapperClass = getMapperClass();
		if (mapperClass == null) {
			return Future.succeededFuture();
		}
		this.beforeSave();
		return DAO.updateWithBLOBs(mapperClass, this);
	}

	public default Future<@Nullable Object> delete() {
		Class<?> mapperClass = getMapperClass();
		if (mapperClass == null) {
			return Future.succeededFuture();
		}
		Object primaryKey = this.primaryKey();
		if (primaryKey.getClass() == Object[].class) {
			return DAO.delete(mapperClass, (Object[]) primaryKey);
		} else {
			return DAO.delete(mapperClass, primaryKey);
		}
	}

	public default Class<?> getMapperClass() {
		return null ; 
	}

	public default void beforeSave() {
	};

	public default Object primaryKey() {
		return null;
	};
}
