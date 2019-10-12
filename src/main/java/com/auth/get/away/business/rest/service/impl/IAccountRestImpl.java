package com.auth.get.away.business.rest.service.impl;

import com.auth.get.away.business.rest.dto.AccountDTO;
import com.auth.get.away.business.rest.service.IAccountRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IAccountRestImpl implements IAccountRest {
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public AccountDTO getAccount(String username) {
       return null;
    }
}
