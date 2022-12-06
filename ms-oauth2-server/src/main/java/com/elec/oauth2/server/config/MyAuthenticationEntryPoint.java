package com.elec.oauth2.server.config;

import com.elec.commons.constant.ApiConstant;
import com.elec.commons.model.domain.ResultInfo;
import com.elec.commons.utils.ResultInfoUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class MyAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // 设置返回 JSON
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

        // 状态码 401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 写出 这段应该是JavaWeb的原生Servlet响应
        PrintWriter out = response.getWriter();
        String errorMessage = authException.getMessage();
        if(StringUtils.isEmpty(errorMessage)) {
            errorMessage = "登陆失效!";
        }
        ResultInfo<String> resultInfo = ResultInfoUtil.buildError(ApiConstant.ERROR_CODE,
                errorMessage, request.getRequestURI());
        out.write(objectMapper.writeValueAsString(resultInfo));
        out.flush();
        out.close();
    }
}
