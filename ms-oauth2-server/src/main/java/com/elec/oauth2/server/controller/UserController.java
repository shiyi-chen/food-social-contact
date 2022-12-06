package com.elec.oauth2.server.controller;

import com.elec.commons.model.domain.ResultInfo;
import com.elec.commons.model.domain.SignInIdentity;
import com.elec.commons.model.vo.SignInDinerInfo;
import com.elec.commons.utils.ResultInfoUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
public class UserController {

    @Resource
    private HttpServletRequest request;

    @Resource
    private RedisTokenStore redisTokenStore;

    /**
     * 获取当前用户信息
     * @param authentication
     * @return
     */
    @GetMapping("user/me")
    public ResultInfo<SignInDinerInfo> getCurrentUser(Authentication authentication) {
        // 获取登录用户的信息，然后设置, 这个强转我们自己写的认证登陆对象
        SignInIdentity signInIdentity = (SignInIdentity) authentication.getPrincipal();
        // 将用户信息转为我们自己的视图对象
        SignInDinerInfo signInDinerInfo = new SignInDinerInfo();
        BeanUtils.copyProperties(signInIdentity, signInDinerInfo);
        // 返回结果集
        return ResultInfoUtil.buildSuccess(request.getServletPath(), signInDinerInfo);
    }

    /**
     * 用户退出
     * @param access_token URL携带access_token
     * @return
     */
    @GetMapping("user/logout")
    public ResultInfo logout(String access_token) {
        // 判断access_token是否为空
        if(StringUtils.isEmpty(access_token)) {
            access_token = request.getHeader("Authorization");
        }
        // 判断authorization是否为空, 既然都不携带令牌默认已经退出成功
        if(StringUtils.isEmpty(access_token)) {
            return ResultInfoUtil.buildSuccess("已退出成功!");
        }
        // 获取token(能走到这部一定有携带token)
        if(access_token.toLowerCase().contains("bearer ".toLowerCase())) {
            access_token = access_token.toLowerCase().replace("bearer ", "");
        }
        // 从redis清除token信息
        OAuth2AccessToken oAuth2AccessToken = redisTokenStore.readAccessToken(access_token);
        if(oAuth2AccessToken != null) {
            redisTokenStore.removeAccessToken(oAuth2AccessToken);
            OAuth2RefreshToken refreshToken = oAuth2AccessToken.getRefreshToken();
            redisTokenStore.removeRefreshToken(refreshToken);
        }
        return ResultInfoUtil.buildSuccess(request.getServletPath(), "退出成功!");
    }

}
