package com.Ashish.Booking.Sytem.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, String> redisTemplate(
            RedisConnectionFactory connectionFactory) {
        RedisTemplate<String,String> rt = new RedisTemplate<>();
        rt.setConnectionFactory(connectionFactory);
        rt.afterPropertiesSet();
        return rt;
    }

    @Bean
    public RedisScript<Long> unlockScript() {

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();

        script.setLocation(new ClassPathResource("scripts/unlock.lua"));

        script.setResultType(Long.class);

        return script;
    }
}
