package cn.jerio.redis.config;

import cn.jerio.serializer.KryoSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.Charset;

/**
 * Created by Jerio on 2019/03/05
 */
@Configuration
public class RedisConfig {

    @Bean(name = "myRedisTemplate")
    RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setEnableTransactionSupport(true);
        StringRedisSerializer keySerializer = new StringRedisSerializer(Charset.forName("utf-8"));
        KryoSerializer<Object> kryoSerializer = new KryoSerializer();
        redisTemplate.setKeySerializer(keySerializer);
        redisTemplate.setHashKeySerializer(keySerializer);
        redisTemplate.setValueSerializer(kryoSerializer);
        redisTemplate.setHashKeySerializer(kryoSerializer);
        return redisTemplate;
    }
}
