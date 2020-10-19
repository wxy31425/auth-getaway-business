package com.auth.get.away.business.rest.service.impl;

import com.auth.get.away.business.rest.dto.AccountDTO;
import com.auth.get.away.business.rest.dto.LoginAccountDTO;
import com.auth.get.away.business.rest.service.IAccountRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class IAccountRestImpl implements IAccountRest {
    @Autowired
    private RestTemplate restTemplate;

    /**
     * 后台服务与服务get调用获取用户账号
     * @param username
     * @return
     */
    @Override
    public AccountDTO getAccount(String username) {
        return restTemplate.getForObject("http://AUTH-GETAWAY-NOTICE/getAccount?username=" + username, AccountDTO.class);
    }

    /**
     * 后台服务与服务之间保存登录信息
     * @param accountId
     * @return
     */
    @Override
    public AccountDTO accountSave(String accountId) {
       return restTemplate.postForObject("http://AUTH-GETAWAY-NOTICE/loginSave/",accountId ,AccountDTO.class);
    }

    /**
     * 后台服务与服务get调用获取登录用户状态
     * @param accountId
     * @return
     */
    @Override
    public LoginAccountDTO getStatus(String accountId) {
        return restTemplate.getForObject("http://AUTH-GETAWAY-NOTICE/getStatus?accountId=" + accountId, LoginAccountDTO.class);
    }


}
