package com.auth.get.away.business.rest.service;


import com.auth.get.away.business.rest.dto.AccountDTO;
import com.auth.get.away.business.rest.dto.LoginAccountDTO;

import java.util.Map;

public interface IAccountRest {
    /**
     * 获取用户管理员账号
     * @param username
     * @return
     */
    AccountDTO getAccount(String username);

    /**
     * 保存登录信息
     * @param accountId
     * @return
     */
    AccountDTO accountSave(String accountId);

    /**
     * 获取登录信息状态
     * @param accountId
     * @return
     */
    LoginAccountDTO getStatus(String accountId);

}
