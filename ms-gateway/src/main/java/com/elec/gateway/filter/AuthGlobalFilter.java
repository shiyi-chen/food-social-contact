package com.elec.gateway.filter;

import com.elec.commons.model.domain.ResultInfo;
import com.elec.gateway.component.HandleException;
import com.elec.gateway.config.IgnoreUrlsConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

/**
 * 网关全局过滤器
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    // 白名单读取配置类
    @Resource
    private IgnoreUrlsConfig ignoreUrlsConfig;

    @Resource
    private RestTemplate restTemplate;
    // 统一异常处理
    @Resource
    private HandleException handleException;

    /**
     * 统一的身份校验处理
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1 是否是白名单
        boolean flag = false;
        AntPathMatcher pathMatcher = new AntPathMatcher();
        String path = exchange.getRequest().getURI().getPath();
        for(String url : ignoreUrlsConfig.getUrls()) {
            if(pathMatcher.match(url, path)) {
                flag = true;
                break;
            }
        }
        // 2 白名单放行
        if(flag) {
            return chain.filter(exchange);
        }
        // 3 是否携带token, 获取token
        String access_token = exchange.getRequest().getQueryParams().getFirst("access_token");
        try {
            if(StringUtils.isEmpty(access_token)) {
                access_token = exchange.getRequest().getHeaders()
                        .get("Authorization").get(0)
                        .replace("Bearer ", "");
            }
        } catch(NullPointerException e) {
            access_token = null;
        }

        // 4 token校验(非空)
        if(StringUtils.isEmpty(access_token)) {
            return handleException.writeError(exchange, "请登录!");
        }
        // 5 携带token向认证授权中心发起请求验证token
        // 5.1 地址:http://localhost:8082/oauth/check_token?token=1c0c85bf-82dc-4915-9d5e-5879631a116c
        String checkTokenUrl = "http://ms-oauth2-server/oauth/check_token?token=".concat(access_token);
        try {
            // 5.2 向授权中心发起token验证
            ResponseEntity<String> entity = restTemplate.getForEntity(checkTokenUrl, String.class);
            log.info("认证结果: {}", entity);
            // 5.3 token无效的几种情况
            if(entity.getStatusCode() != HttpStatus.OK) {
                handleException.writeError(exchange, "Token was not recognised, token: ".concat(access_token));
            }
            if(StringUtils.isEmpty(entity.getBody())) {
                handleException.writeError(exchange, "This token is invalid: ".concat(access_token));
            }
        } catch(Exception e) {
            return handleException.writeError(exchange, "Token was not recognised, token: ".concat(access_token));
        }
        // 6 根据认证中心结果, 是否放行
        return chain.filter(exchange);
    }

    /**
     * 网关过滤器的排序, 数字越小等级越高
     * 目前只有一个过滤器, 就不改了
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
