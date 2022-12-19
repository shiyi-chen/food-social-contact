package com.elec.seckill.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.Collections;
import java.util.UUID;

@Getter
@Setter
public class RedisLock {

    private RedisTemplate redisTemplate;
    private DefaultRedisScript<Long> lockScript;
    private DefaultRedisScript<Object> unlockScript;

    public RedisLock(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        // 加载释放锁的脚本
        this.lockScript = new DefaultRedisScript<>();
        this.lockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lock.lua")));
        this.lockScript.setResultType(Long.class);
        // 加载释放锁的脚本
        this.unlockScript = new DefaultRedisScript<>();
        this.unlockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("unlock.lua")));
    }

    /**
     * 获取锁
     * @param lockName 锁名称
     * @param releaseTime 超时时间(单位:秒)
     * @return key 解锁标识
     */
    public String tryLock(String lockName, long releaseTime) {
        // 存入的线程信息的前缀，防止与其它JVM中线程信息冲突 (即便是同一个线程每次都不一样, 怎么识别是可重入的?)
        String key = UUID.randomUUID().toString();

        // 执行脚本
        Long result = (Long)redisTemplate.execute(
                lockScript,
                Collections.singletonList(lockName),
                key + Thread.currentThread().getId(), releaseTime);

        // 判断结果
        if(result != null && result.intValue() == 1) {
            return key;
        }else {
            return null;
        }
    }
    /**
     * 释放锁
     * @param lockName 锁名称
     * @param key 解锁标识
     */
    public void unlock(String lockName, String key) {
        // 执行脚本
        redisTemplate.execute(
                unlockScript,
                Collections.singletonList(lockName),
                key + Thread.currentThread().getId(), null);
    }

    /**
     *
     * @param lockName 锁名  某某月报:2022:10
     * @param releaseTime 过期时间
     * @return
     */
    public Boolean simpleLock(String lockName, long releaseTime) {
        return (Boolean) redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            // 获取时间毫秒值
            long createTime = System.currentTimeMillis();
            // 获取锁
            Boolean setNX = connection.setNX(lockName.getBytes(), String.valueOf(createTime).getBytes());
            // 设置超时时间
            if (setNX!=null && setNX) {
                connection.setEx(lockName.getBytes(), releaseTime, String.valueOf(createTime).getBytes());
            }
            return setNX;
        });
    }
}