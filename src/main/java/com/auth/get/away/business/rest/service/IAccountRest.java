package com.auth.get.away.business.rest.service;


import com.auth.get.away.business.rest.dto.AccountDTO;

public interface IAccountRest {

    AccountDTO getAccount(String username);
}
