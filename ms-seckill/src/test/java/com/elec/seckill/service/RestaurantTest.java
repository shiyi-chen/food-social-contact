package com.elec.seckill.service;

import com.elec.seckill.RestaurantApplicationTest;
import com.elec.seckill.model.RedisLock;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Slf4j
public class RestaurantTest extends RestaurantApplicationTest {

    private static final String KEY_PREFIX = "yuebao:";

    @Autowired
    RedisTemplate redisTemplate;

    @Test
    public void test1() {
        RedisLock redisLock = new RedisLock(redisTemplate);
        int mouth = 12;
        int dayOfYear = 2022;
        String lockName = KEY_PREFIX + dayOfYear + ":" + mouth;
        Boolean aBoolean = redisLock.simpleLock(lockName, 10000);
        log.info("result: {}", aBoolean);
    }
}