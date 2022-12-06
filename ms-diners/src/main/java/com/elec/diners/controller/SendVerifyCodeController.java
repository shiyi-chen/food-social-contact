package com.elec.diners.controller;

import com.elec.commons.model.domain.ResultInfo;
import com.elec.commons.utils.ResultInfoUtil;
import com.elec.diners.service.SendVerifyCodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
public class SendVerifyCodeController {

    @Resource
    private SendVerifyCodeService sendVerifyCodeService;

    @Resource
    private HttpServletRequest request;

    @GetMapping("send")
    public ResultInfo<String> send(String phone) {
        sendVerifyCodeService.send(phone);
        return ResultInfoUtil.buildSuccess("验证码发送成功!", request.getServletPath());
    }
}
