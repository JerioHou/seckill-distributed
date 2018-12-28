package cn.jerio.serializer;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Jerio on 2018/12/28
 */
public class KryoSerializer <T> implements RedisSerializer<T>  {
    private final static Logger logger = LoggerFactory.getLogger(KryoSerializer.class);
    private static final ThreadLocal<Kryo> kryos = new ThreadLocal<Kryo>() {
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            return kryo;
        };
    };
    @Override
    public byte[] serialize(Object obj) throws SerializationException {
        if (obj == null) {
            throw new RuntimeException("serialize param must not be null");
        }
        Kryo kryo = kryos.get();
        Output output = new Output(64, -1);
        try {
            kryo.writeClassAndObject(output, obj);
            return output.toBytes();
        } finally {
            closeOutputStream(output);
        }
    }
    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null) {
            return null;
        }
        Kryo kryo = kryos.get();
        Input input = null;
        try {
            input = new Input(bytes);
            return (T) kryo.readClassAndObject(input);
        } finally {
            closeInputStream(input);
        }
    }
    private static void closeOutputStream(OutputStream output) {
        if (output != null) {
            try {
                output.flush();
                output.close();
            } catch (Exception e) {
                logger.error("serialize object close outputStream exception", e);
            }
        }
    }
    private static void closeInputStream(InputStream input) {
        if (input != null) {
            try {
                input.close();
            } catch (Exception e) {
                logger.error("serialize object close inputStream exception", e);
            }
        }
    }

}
