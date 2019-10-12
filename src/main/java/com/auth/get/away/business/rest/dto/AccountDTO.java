package com.auth.get.away.business.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountDTO {

    private String id;

    /**
     * 商家Id
     */
    private String storeId;

    /**
     * 账户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;
    /**
     * 头像
     */
    private String avatar;


    /**
     * 账户所有者
     */
    private String role;

    /**
     * 绑定令牌key
     */
    private String key;
}
