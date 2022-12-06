package com.imooc.oauth2.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.elec.commons.model.pojo.Diners;
import com.elec.oauth2.server.Oauth2ServerApplication;
import com.elec.oauth2.server.config.ClientOAuth2DataConfiguration;
import com.elec.oauth2.server.mapper.DinersMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.Resource;

@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@SpringBootTest(classes = OAuth2ServerApplicationTests.class)
public class OAuth2ServerApplicationTests {

    @Resource
    protected MockMvc mockMvc;

    @Resource
    private DinersMapper dinersMapper;

    @Resource
    private ClientOAuth2DataConfiguration clientOAuth2DataConfiguration;

    @Test
    public void findUser() {
        LambdaQueryWrapper<Diners> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Diners::getUsername, "test")
                .or().eq(Diners::getPhone, "test")
                .or().eq(Diners::getEmail, "test");
        Diners diners = this.dinersMapper.selectOne(queryWrapper);
        System.out.println(diners);
        System.out.println(clientOAuth2DataConfiguration);
    }

}
