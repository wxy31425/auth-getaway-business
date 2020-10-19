package com.auth.get.away.business.rest.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class LoginAccountDTO {
    /**
     * 账号Id
     */
    private String accountId;
    /**
     * 账号状态
     */
    private Integer status;
}
