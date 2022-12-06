package com.elec.oauth2.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.elec.commons.model.domain.SignInIdentity;
import com.elec.commons.model.pojo.Diners;
import com.elec.commons.utils.AssertUtil;
import com.elec.oauth2.server.mapper.DinersMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserService implements UserDetailsService {

    @Resource
    private DinersMapper dinersMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 用户名不能为空
        AssertUtil.isNotEmpty(username, "请输入用户名");
        // 根据用户名查找用户信息
        LambdaQueryWrapper<Diners> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Diners::getUsername, username)
                .or().eq(Diners::getPhone, username)
                .or().eq(Diners::getEmail, username);
        Diners diners = this.dinersMapper.selectOne(queryWrapper);
        if(diners == null) {
            throw new UsernameNotFoundException("用户名或密码错误, 请重新输入!");
        }
        // 初始化自定义登陆认证对象
        SignInIdentity signInIdentity = new SignInIdentity();
        BeanUtils.copyProperties(diners, signInIdentity);
        // 返回自定义认证登陆对象
        return signInIdentity;
        // 用户名 密码 角色 这个User是框架的, 也是实现了UserDetails
        /*return new User(username, diners.getPassword(),
                AuthorityUtils.commaSeparatedStringToAuthorityList(diners.getRoles()));*/
    }
}
