package com.Ashish.Booking.Sytem.RedisManagement;


import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

@Service
public class RedisLockService {
    private final RedisTemplate<String,String> redisTemplate;

    private final RedisScript<Long> unlockScript;

    public RedisLockService(
            RedisTemplate<String,String> redisTemplate,RedisScript<Long> unlockScript){

        this.redisTemplate = redisTemplate;
        this.unlockScript = unlockScript;
    }

    private static final Duration DEFAULT_TTL =
            Duration.ofMinutes(5);

    public LockResult lock(String key){

        String token = UUID.randomUUID().toString();

        Boolean success = redisTemplate.opsForValue().setIfAbsent(key,token,DEFAULT_TTL);

        if(!success){
            return new LockResult(false,null);
        }
        return new LockResult(true,token);

    }


    public LockInfo get(String key){
        String token = redisTemplate.opsForValue().get(key);

        if(token == null){
            return new LockInfo(false, null);
        }

        return new LockInfo(true, token);
    }

    public boolean unlock(String key,String token){

//        redisTemplate.delete(key);
        Long result = redisTemplate.execute(
                unlockScript,
                Collections.singletonList(key),
                token
        );

        return result != null && result == 1;
    }

}
