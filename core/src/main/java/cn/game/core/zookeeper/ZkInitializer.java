package cn.game.core.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorTransactionFinal;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.util.*;

/**
 * 运维/工具侧的一次性写入辅助：
 * - initOnce：仅当 basePath 无任何子节点时，批量创建
 * - initMissing：仅创建缺失节点（半幂等）
 * - upsertAll：批量 upsert
 *
 * 为简化：不做并发版本控制，不处理强原子性失败回滚等高级特性。
 */
public class ZkInitializer<K, T> {

    private final CuratorFramework client;
    private final PathPolicy pathPolicy;
    private final ValueCodec<T> codec;
    private final IdExtractor<T> idExtractor;
    private final KeyAdapter<K> keyAdapter;

    public ZkInitializer(CuratorFramework client,
                         PathPolicy pathPolicy,
                         ValueCodec<T> codec,
                         IdExtractor<T> idExtractor,
                         KeyAdapter<K> keyAdapter) {
        this.client = Objects.requireNonNull(client, "client");
        this.pathPolicy = Objects.requireNonNull(pathPolicy, "pathPolicy");
        this.codec = Objects.requireNonNull(codec, "codec");
        this.idExtractor = Objects.requireNonNull(idExtractor, "idExtractor");
        this.keyAdapter = Objects.requireNonNull(keyAdapter, "keyAdapter");
    }

    public boolean initOnce(Collection<T> values) throws Exception {
        Objects.requireNonNull(values, "values");
        ensureRoot();

        List<String> children;
        try {
            children = client.getChildren().forPath(pathPolicy.basePath());
            if (children != null && !children.isEmpty()) {
                return false;
            }
        } catch (KeeperException.NoNodeException e) {
            ensureRoot();
        }

        // 再次探测是否任何目标已存在
        for (T v : values) {
            String sk = toStringKey(v);
            if (client.checkExists().forPath(pathPolicy.pathForId(sk)) != null) {
                return false;
            }
        }

        // 事务批量创建（简化）
        CuratorTransactionFinal tx = client.inTransaction().check().forPath(pathPolicy.basePath()).and();
        for (T v : values) {
            String sk = toStringKey(v);
            byte[] data = codec.encode(v);
            tx = tx.create().forPath(pathPolicy.pathForId(sk), data).and();
        }
        tx.commit();
        return true;
    }

    public List<String> initMissing(Collection<T> values) throws Exception {
        Objects.requireNonNull(values, "values");
        ensureRoot();

        List<String> created = new ArrayList<>();
        for (T v : values) {
            String sk = toStringKey(v);
            String path = pathPolicy.pathForId(sk);
            if (client.checkExists().forPath(path) != null) continue;
            try {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, codec.encode(v));
                created.add(sk);
            } catch (KeeperException.NodeExistsException ignore) {
            }
        }
        return created;
    }

    public void upsertAll(Collection<T> values) throws Exception {
        Objects.requireNonNull(values, "values");
        CuratorTransactionFinal tx = client.inTransaction().check().forPath(pathPolicy.basePath()).and();
        for (T v : values) {
            String sk = toStringKey(v);
            String path = pathPolicy.pathForId(sk);
            byte[] data = codec.encode(v);
            if (client.checkExists().forPath(path) == null) {
                tx = tx.create().forPath(path, data).and();
            } else {
                tx = tx.setData().forPath(path, data).and();
            }
        }
        tx.commit();
    }

    private String toStringKey(T value) {
        Object idObj = idExtractor.getId(value);
        @SuppressWarnings("unchecked")
        String sk = keyAdapter.toStringKey((K) idObj);
        return Objects.requireNonNull(sk, "id(key) must not be null");
    }

    private void ensureRoot() {
        try {
            if (client.checkExists().forPath(pathPolicy.basePath()) == null) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(pathPolicy.basePath());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to ensure root: " + pathPolicy.basePath(), e);
        }
    }
}

