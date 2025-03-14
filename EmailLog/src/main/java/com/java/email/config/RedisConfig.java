//package com.java.email.config;
//
//import com.java.email.utils.LogUtil;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//@Configuration
//public class RedisConfig {
//    private static final LogUtil logUtil = LogUtil.getLogger(RedisConfig.class);
//
//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
//        logUtil.info("RedisConfig.redisTemplate(),进入RedisTemplate");
//        // 创建 RedisTemplate 对象
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        // 设置连接工厂
//        template.setConnectionFactory(factory);
//
//        // 使用 StringRedisSerializer 来序列化和反序列化 Redis 的 key 值
//        StringRedisSerializer stringSerializer = new StringRedisSerializer();
//        // 使用 GenericJackson2JsonRedisSerializer 来序列化和反序列化 Redis 的 value 值
//        GenericJackson2JsonRedisSerializer jacksonSerializer = new GenericJackson2JsonRedisSerializer();
//
//        // 设置 key 和 hash key 的序列化方式
//        template.setKeySerializer(stringSerializer);
//        template.setHashKeySerializer(stringSerializer);
//
//        // 设置 value 和 hash value 的序列化方式
//        template.setValueSerializer(jacksonSerializer);
//        template.setHashValueSerializer(jacksonSerializer);
//
//        template.afterPropertiesSet();
//        return template;
//    }
//}

package com.java.email.config;

import com.java.email.utils.LogUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;

@Configuration
public class RedisConfig {
    private static final LogUtil logUtil = LogUtil.getLogger(RedisConfig.class);
    @Value("${spring.data.redis.host}")
    private String redisHost;
    @Value("${spring.data.redis.port}")
    private int redisPort;
    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        logUtil.info("RedisConfig.redisTemplate(),进入RedisTemplate");
        // 创建 RedisTemplate 对象
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置连接工厂
        template.setConnectionFactory(factory);

        // 使用 StringRedisSerializer 来序列化和反序列化 Redis 的 key 值
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        // 使用 GenericJackson2JsonRedisSerializer 来序列化和反序列化 Redis 的 value 值
        GenericJackson2JsonRedisSerializer jacksonSerializer = new GenericJackson2JsonRedisSerializer();

        // 设置 key 和 hash key 的序列化方式
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // 设置 value 和 hash value 的序列化方式
        template.setValueSerializer(jacksonSerializer);
        template.setHashValueSerializer(jacksonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // 配置 RedisStandaloneConfiguration 来设置 Redis 的主机、端口和密码
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(redisHost); // Redis 服务器地址
        configuration.setPort(redisPort); // Redis 端口
        configuration.setPassword(redisPassword); // 设置密码

        // 创建 LettuceConnectionFactory
        return new LettuceConnectionFactory(configuration);
    }
}


