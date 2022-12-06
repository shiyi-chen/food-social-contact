package com.elec.diners.service;

import cn.hutool.core.util.RandomUtil;
import com.elec.commons.constant.RedisKeyConstant;
import com.elec.commons.utils.AssertUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class SendVerifyCodeService {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    public void send(String phone) {
        // 检查手机号是否合法
        AssertUtil.isNotEmpty(phone, "手机号不能为空");
        // 判断该手机号对是否生成过验证码
        if(!checkCodeIsExpired(phone)) {
            return;
        }
        // 生成6位验证码
        String code = RandomUtil.randomNumbers(6);
        // TODO 向手机发送验证码
        // 将验证码保存至Redis, 过期时间60秒
        String key = RedisKeyConstant.verify_code.getKey().concat(phone);
        redisTemplate.opsForValue().set(key, code, 60, TimeUnit.SECONDS);
        String s = redisTemplate.opsForValue().get(key);
    }


    /**
     * 检验手机号是否生成过验证码
     * @param phone 用户手机号
     * @return
     */
    private boolean checkCodeIsExpired(String phone) {
        String key = RedisKeyConstant.verify_code.getKey().concat(phone);
        String code = redisTemplate.opsForValue().get(key);
        return StringUtils.isEmpty(code);
    }

    /**
     * 根据手机号获取验证码
     * @param phone
     * @return
     */
    public String getCodeByPhone(String phone) {
        String key = RedisKeyConstant.verify_code.getKey().concat(phone);
        return redisTemplate.opsForValue().get(key);
    }
}
