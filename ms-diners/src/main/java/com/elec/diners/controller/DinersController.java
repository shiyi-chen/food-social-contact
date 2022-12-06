package com.elec.diners.controller;

import com.elec.commons.constant.ApiConstant;
import com.elec.commons.model.domain.ResultInfo;
import com.elec.commons.model.dto.DinersDTO;
import com.elec.commons.model.vo.ShortDinerInfo;
import com.elec.commons.utils.AssertUtil;
import com.elec.commons.utils.ResultInfoUtil;
import com.elec.diners.service.DinersService;
import com.elec.diners.vo.LoginDinerInfo;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@Api(value = "食客相关接口", tags = "食客相关接口")
public class DinersController {

    @Resource
    private DinersService dinersService;

    @Resource
    private HttpServletRequest request;


    /**
     * 根据 ids 查询食客信息
     *
     * @param ids
     * @return
     */
    @GetMapping("findByIds")
    public ResultInfo<List<ShortDinerInfo>> findByIds(String ids) {
        List<ShortDinerInfo> dinerInfos = dinersService.findByIds(ids);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), dinerInfos);
    }


    //注册
    @PostMapping("register")
    public ResultInfo<LoginDinerInfo> register(@RequestBody DinersDTO dinersDTO) {
        return dinersService.register(dinersDTO, request.getServletPath());
    }

    //检验手机是否注册过
    @GetMapping("checkPhone")
    public ResultInfo<String> checkPhone(String phone) {
        dinersService.checkPhoneIsRegistered(phone);
        return ResultInfoUtil.buildSuccess(ApiConstant.SUCCESS_MESSAGE, request.getServletPath());
    }

    //登陆
    @GetMapping("signin")
    public ResultInfo<LoginDinerInfo> signIn(String account, String password) {
        return dinersService.signIn(account, password, request.getServletPath());
    }
}
