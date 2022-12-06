package com.elec.oauth2.server.config;

import cn.hutool.crypto.digest.DigestUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.annotation.Resource;

/**
 * security配置类
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    // 注入 Redis 连接工厂
    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    // 初始化 RedisTokenStore 用于将token存储至Redis
    @Bean
    public RedisTokenStore redisTokenStore() {
        RedisTokenStore redisTokenStore = new RedisTokenStore(redisConnectionFactory);
        // 设置前缀
        redisTokenStore.setPrefix("TOKEN:");
        return redisTokenStore;
    }

    // 初始化密码编码器, 用 MD5 加密密码
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            /**
             * 加密用的
             * @param rawPassword   原始密码
             * @return 加密后密码
             */
            @Override
            public String encode(CharSequence rawPassword) {
                // 使用hutool工具集进行MD5加密
                return DigestUtil.md5Hex(rawPassword.toString());
            }

            /**
             * 解密用的
             * @param rawPassword   原始密码
             * @param encodedPassword   加密后密码
             * @return  是否一致
             */
            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return DigestUtil.md5Hex(rawPassword.toString())
                        .equals(encodedPassword);
            }
        };
    }

    // 初始化认证管理对象(重写)
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    // 放行和认证规则(重写)
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // csrf默认只接收get请求, 为了便于测试, 其他请求类型也放行
        http.csrf().disable()
                // 增加放行规则1
                .authorizeRequests()
                // oauth与actuator等权限请求放行
                .antMatchers("/oauth/**", "/actuator/**").permitAll()
                // 其他放行规则
                .and()
                .authorizeRequests()
                // 其他请求必须认证才能访问
                .anyRequest().authenticated();
    }
}
