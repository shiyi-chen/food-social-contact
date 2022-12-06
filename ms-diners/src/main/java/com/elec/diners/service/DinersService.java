package com.elec.diners.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elec.commons.constant.ApiConstant;
import com.elec.commons.model.domain.ResultInfo;
import com.elec.commons.model.dto.DinersDTO;
import com.elec.commons.model.pojo.Diners;
import com.elec.commons.model.vo.ShortDinerInfo;
import com.elec.commons.utils.AssertUtil;
import com.elec.commons.utils.ResultInfoUtil;
import com.elec.diners.config.OAuth2ClientConfiguration;
import com.elec.diners.domain.OAuthDinerInfo;
import com.elec.diners.mapper.DinersMapper;
import com.elec.diners.vo.LoginDinerInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 食客服务层逻辑
 */
@Service
public class DinersService {

    // 模拟form发起认证授权所需
    @Resource
    private RestTemplate restTemplate;
    // 认证授权的一些参数, 由配置类读取
    @Resource
    private OAuth2ClientConfiguration oAuth2ClientConfiguration;
    // 认证授权中心的跟地址
    @Value("${service.name.ms-oauth-server}")
    private String oauthServerName;

    @Resource
    private DinersMapper dinersMapper;

    @Resource
    private SendVerifyCodeService sendVerifyCodeService;

    /**
     * 根据 ids 查询食客信息
     *
     * @param ids 主键 id，多个以逗号分隔，逗号之间不用空格
     * @return
     */
    public List<ShortDinerInfo> findByIds(String ids) {
        AssertUtil.isNotEmpty(ids);
        List<Integer> idArr = Arrays.stream(ids.split(","))
                .map(Integer::parseInt).collect(Collectors.toList());
        List<ShortDinerInfo> dinerInfos = dinersMapper.findByIds(idArr);
        return dinerInfos;
    }

    /**
     * 用户注册
     * @param dinersDTO
     * @param path
     * @return
     */
    public ResultInfo<LoginDinerInfo> register(DinersDTO dinersDTO, String path) {
        // 参数非空校验
        String username = dinersDTO.getUsername();
        AssertUtil.isNotEmpty(username, "请输入用户名");
        String password = dinersDTO.getPassword();
        AssertUtil.isNotEmpty(password, "请输入密码");
        String phone = dinersDTO.getPhone();
        AssertUtil.isNotEmpty(phone, "请输入手机号");
        String verifyCode = dinersDTO.getVerifyCode();
        AssertUtil.isNotEmpty(verifyCode, "请输入验证码");
        // 验证码判断 (获取 -> 是否过期 -> 是否一致)
        String code = sendVerifyCodeService.getCodeByPhone(phone);
        AssertUtil.isNotEmpty(code, "验证码已过期, 请重新发送");
        AssertUtil.isTrue(!verifyCode.equals(code), "验证码不一致, 请重新输入");
        // 确认用户名不存在
        LambdaQueryWrapper<Diners> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Diners::getUsername, username.trim());
        Diners diners = dinersMapper.selectOne(wrapper);
        AssertUtil.isTrue(diners != null, "用户名已注册, 请重新输入");
        // 注册 : {密码加密 -> 插入数据}
        dinersDTO.setPassword(DigestUtil.md5Hex(password));
        Diners newDiners = Diners.builder()
                .createDate(new Date())
                .updateDate(new Date())
                .isValid(1)
                .roles("ROLE_USER")
                .build();
        BeanUtil.copyProperties(dinersDTO, newDiners, false);
        dinersMapper.insert(newDiners);
        // 注册后自动登录
        return signIn(username, password, path);
    }

    /**
     * 校验手机号是否被注册
     * @param phone
     */
    public void checkPhoneIsRegistered(String phone) {
        // 参数非空校验
        AssertUtil.isNotEmpty(phone, "手机号不能为空!");
        // 根据手机号查找食客信息
        LambdaQueryWrapper<Diners> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Diners::getPhone, phone);
        Diners diners = dinersMapper.selectOne(wrapper);
        // 如果查到到就是已注册过
        AssertUtil.isTrue(diners == null, "该手机未注册过!");
        // 是否可用
        AssertUtil.isTrue(diners.getIsValid() == 0, "该用户被锁定无法使用!");
    }

    /**
     * 用户登陆
     * @param account 账号
     * @param password 密码
     * @param path 请求路径
     * @return
     */
    public ResultInfo<LoginDinerInfo> signIn(String account, String password, String path) {
        // 1 参数校验
        AssertUtil.isNotEmpty(account, "请输入登陆账号");
        AssertUtil.isNotEmpty(account, "请输入登录密码");
        // 2 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // 3 构建请求体
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("username", account);
        body.add("password", password);
        body.setAll(BeanUtil.beanToMap(oAuth2ClientConfiguration));
        // 4 设置 Authorization 认证方式
        restTemplate.getInterceptors()
                .add(new BasicAuthenticationInterceptor(
                        oAuth2ClientConfiguration.getClientId(),
                        oAuth2ClientConfiguration.getSecret()));
        // 5 构建完整的请求 这里泛型是body的type
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(body, headers);
        // 6 发送请求
        ResponseEntity<ResultInfo> result = restTemplate.postForEntity(
                oauthServerName + "oauth/token", httpEntity, ResultInfo.class);
        // 7 处理返回结果
        // 7.1 健壮性处理
        AssertUtil.isTrue(result.getStatusCode() != HttpStatus.OK, "登陆失败!");
        ResultInfo resultInfo = result.getBody();
        if(resultInfo.getCode() != ApiConstant.SUCCESS_CODE) {
            // 登陆失败
            resultInfo.setData(resultInfo.getMessage());
            return resultInfo;
        }
        // 7.2 通过域对象接收返回结果原先是一个MAP, 使用hutool工具及转转
        OAuthDinerInfo dinerInfo = BeanUtil.fillBeanWithMap(
                (LinkedHashMap)resultInfo.getData(), new OAuthDinerInfo(), false);
        // 7.3 将域对象 转 视图对象, 返回给前端
        LoginDinerInfo loginDinerInfo = LoginDinerInfo.builder()
                .avatarUrl(dinerInfo.getAvatarUrl())
                .nickname(dinerInfo.getNickname())
                .token(dinerInfo.getAccessToken())
                .build();
        return ResultInfoUtil.buildSuccess(path, loginDinerInfo);
    }
}
