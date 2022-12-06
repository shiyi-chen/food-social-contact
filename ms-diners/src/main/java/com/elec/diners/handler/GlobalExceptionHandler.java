package com.elec.diners.handler;

import com.elec.commons.exception.ParameterException;
import com.elec.commons.model.domain.ResultInfo;
import com.elec.commons.utils.ResultInfoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Resource
    private HttpServletRequest request;

    @ExceptionHandler(ParameterException.class)
    public ResultInfo<Map<String, String>> handlerParameterException(ParameterException ex) {
        log.info("发生自定义异常: {}", ex.getMessage());
        String path = request.getRequestURI();
        return ResultInfoUtil.buildError(ex.getErrorCode(), ex.getMessage(), path);
    }

    @ExceptionHandler(Exception.class)
    public ResultInfo<Map<String, String>> handlerException(Exception ex) {
        ex.printStackTrace();
        log.info("未知异常: {}", ex.getMessage());
        String path = request.getRequestURI();
        return ResultInfoUtil.buildError(path);
    }
}
