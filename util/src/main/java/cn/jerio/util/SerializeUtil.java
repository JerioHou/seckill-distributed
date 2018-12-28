package cn.jerio.util;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

/**
 * Created by Jerio on 2018/12/28
 */
public class SerializeUtil {

    public static <T> byte[] toByteArray(T t,Class<T> typeClass) {
        RuntimeSchema<T> schema = RuntimeSchema.createFrom(typeClass);
        return ProtostuffIOUtil.toByteArray(t,schema,
                LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
    }

    public static <T> T toObject(byte[] bytes,Class<T> typeClass) {
        RuntimeSchema<T> schema = RuntimeSchema.createFrom(typeClass);
        T t = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(bytes, t, schema);
        return t;
    }
}
