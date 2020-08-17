package com.geekouc.babytun;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@SpringBootApplication
@MapperScan("com.geekouc.babytun") //mybatis在springboot启动的时候自动扫描mybatis实现的mapper接口
@EnableCaching //开启声明式缓存:利用注解来控制缓存的使用
public class BabytunApplication {

    @Bean //自定义一个RedisTemplate,更改其序列化方式为JSON.而不使用spring默认的JDK序列化
    public RedisTemplate<Object,Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<Object,Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class));
        return redisTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(BabytunApplication.class, args);
    }
}
