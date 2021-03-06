package com.auth.get.away.business.security;

import com.auth.get.away.business.rest.dto.AccountDTO;
import com.auth.get.away.business.rest.service.IAccountRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import java.util.Arrays;

/**
 * Authenticate a user from the database.
 */

@Component("userDetailsService")
public class DomainUserDetailsService implements UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(DomainUserDetailsService.class);

    @Autowired
    private IAccountRest accountRest;

    @Override
    public UserDetails loadUserByUsername(final String username) {
            AccountDTO accountDTO = accountRest.getAccount(username);
            if (accountDTO == null) {
                throw new UsernameNotFoundException("没有找到该邮箱为:" + username + "的登录账号");
            }
            return new org.springframework.security.core.userdetails.User(accountDTO.getEmail(),
                    accountDTO.getPassword(),
                    Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
    }


}
