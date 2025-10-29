package cn.game.core.db;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Writer;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 将任意 Java 对象以 JSON 流式写入 JDBC 的 CLOB/TEXT/LONGTEXT 字段，避免在 JVM 内部构造巨大字符串。
 *
 * 使用方式（示例，见下文 XML 修改）：
 *   #{modulesObj, jdbcType=LONGVARCHAR, typeHandler=cn.game.core.db.JsonClobStreamingTypeHandler}
 *
 * 说明：
 * - setNonNullParameter：使用 Jackson 将 parameter 流式写入到 PipedWriter，另一端作为 Reader 传给 JDBC。
 * - getNullableResult：读取时仍返回 String（交给默认映射即可），本 Handler 仅用于写入。
 *
 * 依赖：
 * - 复用你项目中的 Jackson ObjectMapper（推荐从你的 JsonUtil 获取），
 *   如果无法静态获取，也可以 new ObjectMapper()，但建议与业务保持一致配置。
 */
public class JsonClobStreamingTypeHandler extends BaseTypeHandler<Object> {

    // 可替换为你项目的工具类 ObjectMapper，例如：cn.game.util.JsonUtil.getObjectMapper()
    // 这里直接 new 一个也可以，但推荐根据你的项目情况调整：
    private static final ObjectMapper MAPPER;
    static {
        ObjectMapper mapper;
        try {
            // 尝试通过反射调用你的 JsonUtil.getObjectMapper()
            Class<?> util = Class.forName("cn.game.util.JsonUtil");
            mapper = (ObjectMapper) util.getMethod("getObjectMapper").invoke(null);
        } catch (Throwable ignore) {
            mapper = new ObjectMapper(); // 兜底
        }
        MAPPER = mapper;
    }

    // 管道缓冲区大小，按需调整（越大写入越顺畅，但会多占用一些堆内存）
    private static final int PIPE_BUFFER = 64 * 1024;

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
            throws SQLException {

        final PipedWriter pw = new PipedWriter();
        final PipedReader pr;
        try {
            pr = new PipedReader(pw, PIPE_BUFFER);
        } catch (IOException e) {
            throw new SQLException("Failed to create pipe for JSON streaming", e);
        }

        // 启动后台线程，将对象以 JSON 流式写入 pw
        Thread writerThread = new Thread(() -> {
            try (Writer w = pw) {
                // 如果传入的本就是 String，直接写，避免再次 JSON 编码
                if (parameter instanceof CharSequence cs) {
                    w.write(cs.toString());
                } else {
                    MAPPER.writeValue(w, parameter);
                }
                w.flush();
            } catch (Exception e) {
                // 这里不抛出，让 JDBC 在读取 pr 时感知异常（语句会失败）
                try {
                    pw.close();
                } catch (IOException ignored) {}
            }
        }, "json-stream-writer");
        writerThread.setDaemon(true);
        writerThread.start();

        // 不传长度，驱动将流式发送（MySQL 8.x 驱动可用）
        ps.setCharacterStream(i, pr);
    }

    // 读取路径不使用该 Handler，下面实现仅为完整性（返回 String）
    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getString(columnName);
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getString(columnIndex);
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return cs.getString(columnIndex);
    }
}