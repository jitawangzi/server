package cn.game.core.net.vertx.codec;

import com.google.protobuf.Message;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.util.KryoUtils;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.protocol.BaseProtocol;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

public class UniversalMessageCodec implements MessageCodec<Object, Object> {

    private static final byte TYPE_PROTOBUF_WITH_MSGID = 1;
    private static final byte TYPE_PROTOBUF_WITH_CLASSNAME = 2;
    private static final byte TYPE_KRYO_OBJECT = 3;
    private static final byte TYPE_IPROTOCOL = 4;

    @Override
    public void encodeToWire(Buffer buffer, Object obj) {
        if (obj instanceof IProtocol) {
            buffer.appendByte(TYPE_IPROTOCOL);
            IProtocol<?> protocol = (IProtocol<?>) obj;

            // 1. 写协议类名
            String className = obj.getClass().getName();
            byte[] classNameBytes = className.getBytes(StandardCharsets.UTF_8);
            buffer.appendInt(classNameBytes.length);
            buffer.appendBytes(classNameBytes);

            // 2. 写基础属性
            buffer.appendInt(protocol.getMsgID());
            buffer.appendInt(protocol.getErrorCode());
            // 可按需添加更多协议头字段

            // 3. 写body字节
            byte[] data = protocol.serializeData();
            if (data != null && data.length > 0) {
                buffer.appendInt(data.length);
                buffer.appendBytes(data);
            } else {
                buffer.appendInt(0);
            }
        } else if (obj instanceof Message) {
            Integer msgId = PbProtocol.getInstance().getMsgIdOrNull(obj.getClass().getSimpleName());
            if (msgId != null && msgId > 0) {
                buffer.appendByte(TYPE_PROTOBUF_WITH_MSGID);
                buffer.appendInt(msgId);
                buffer.appendBytes(((Message) obj).toByteArray());
            } else {
                buffer.appendByte(TYPE_PROTOBUF_WITH_CLASSNAME);
                byte[] classNameBytes = obj.getClass().getName().getBytes(StandardCharsets.UTF_8);
                buffer.appendInt(classNameBytes.length);
                buffer.appendBytes(classNameBytes);
                buffer.appendBytes(((Message) obj).toByteArray());
            }
        } else {
            buffer.appendByte(TYPE_KRYO_OBJECT);
            byte[] bytes = KryoUtils.serializeClassAndObject(obj);
            buffer.appendInt(bytes.length);
            buffer.appendBytes(bytes);
        }
    }

    @Override
    public Object decodeFromWire(int pos, Buffer buffer) {
        byte type = buffer.getByte(pos);
        pos += 1;

        if (type == TYPE_IPROTOCOL) {
            // 1. 协议类名
            int classNameLen = buffer.getInt(pos); pos += 4;
            String className = buffer.getString(pos, pos + classNameLen, StandardCharsets.UTF_8.name());
            pos += classNameLen;

            try {
                Class<?> clazz = Class.forName(className);
                IProtocol<?> protocol = (IProtocol<?>) clazz.getDeclaredConstructor().newInstance();

                // 2. 基础属性
                int msgId = buffer.getInt(pos); pos += 4;
                int errorCode = buffer.getInt(pos); pos += 4;

                // 3. 读取body
                int dataLength = buffer.getInt(pos); pos += 4;
                byte[] data = null;
                if (dataLength > 0) {
                    data = buffer.getBytes(pos, pos + dataLength);
                    pos += dataLength;
                }

                // 设置基础属性
                if (protocol instanceof BaseProtocol) {
                    ((BaseProtocol) protocol).setMsgID(msgId);
                    ((BaseProtocol) protocol).setErrorCode(errorCode);
                }

                protocol.deserializeData(data);

                return protocol;
            } catch (Exception e) {
                throw new RuntimeException("Failed to decode protocol", e);
            }

        } else if (type == TYPE_PROTOBUF_WITH_MSGID) {
            int msgId = buffer.getInt(pos); pos += 4;
            byte[] bytes = buffer.getBytes(pos, buffer.length());
            return PbProtocol.getInstance().parseFrom(msgId, bytes);

        } else if (type == TYPE_PROTOBUF_WITH_CLASSNAME) {
            int classNameLen = buffer.getInt(pos); pos += 4;
            String className = buffer.getString(pos, pos + classNameLen, StandardCharsets.UTF_8.name());
            pos += classNameLen;
            byte[] bytes = buffer.getBytes(pos, buffer.length());
            try {
                Class<?> clazz = Class.forName(className);
                Method parseFrom = clazz.getMethod("parseFrom", byte[].class);
                return parseFrom.invoke(null, bytes);
            } catch (Exception e) {
                throw new RuntimeException("Protobuf parse error for class: " + className, e);
            }

        } else if (type == TYPE_KRYO_OBJECT) {
            int length = buffer.getInt(pos); pos += 4;
            byte[] bytes = buffer.getBytes(pos, pos + length);
            return KryoUtils.deserializeClassAndObject(bytes);

        } else {
            throw new IllegalArgumentException("Unknown message type: " + type);
        }
    }

    @Override
    public Object transform(Object obj) {
        return obj;
    }

    @Override
    public String name() {
        return "UniversalMessage";
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}