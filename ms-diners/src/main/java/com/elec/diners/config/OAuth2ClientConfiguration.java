package com.elec.diners.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "oauth2.client")
public class OAuth2ClientConfiguration {

    private String clientId;
    private String secret;
    // 注意这里不能写驼峰命名, 因为Security默认http请求就是带这个格式的参数
    private String grant_type;
    private String scope;

}
