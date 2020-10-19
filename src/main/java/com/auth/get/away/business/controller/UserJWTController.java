package com.auth.get.away.business.controller;

import com.auth.get.away.business.config.Encrypt;
import com.auth.get.away.business.controller.vm.LoginVM;
import com.auth.get.away.business.controller.vm.RegisterVM;
import com.auth.get.away.business.controller.vm.UpdatePasswordVM;
import com.auth.get.away.business.rest.dto.AccountDTO;
import com.auth.get.away.business.rest.dto.LoginAccountDTO;
import com.auth.get.away.business.rest.service.IAccountRest;
import com.auth.get.away.business.security.DomainUserDetailsService;
import com.auth.get.away.business.security.jwt.TokenProvider;
import com.boostor.framework.rest.ResposeStatus;
import com.boostor.framework.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.HashOperations;
import javax.servlet.http.HttpServletRequest;

/**
 * Controller to authenticate users.
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class UserJWTController {
    private final RestTemplate restTemplate;
    private final StringRedisTemplate redisTemplate;
    private final IAccountRest accountRest;

    public UserJWTController(RestTemplate restTemplate,
                             IAccountRest accountRest, StringRedisTemplate redisTemplate, DomainUserDetailsService domainUserDetailsService) {
        this.accountRest = accountRest;
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
    }
    /**
     * 登录
     * @param loginVM
     * @return
     */
    @PostMapping("/authenticate")
    public ResposeStatus authorize(@RequestBody LoginVM loginVM, HttpServletRequest request) throws Exception {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginVM.getUsername(), loginVM.getPassword());
        AccountDTO accountDTO = accountRest.getAccount(loginVM.getUsername());
        LoginAccountDTO loginAccountDTO = accountRest.getStatus(accountDTO.getId());
        if (accountDTO == null) {
            return ResposeStatus.error("邮箱账号错误","201");
        }
        if (!Encrypt.encryptRMS(authenticationToken.getCredentials().toString()).equals(accountDTO.getPassword())) {
            return ResposeStatus.error("该邮箱密码错误","202");
        }
        if(loginAccountDTO.getStatus() == 0){
            return ResposeStatus.error("该登录账号被禁用","207");
        }
        accountRest.accountSave(accountDTO.getId());
        String username = (String) authenticationToken.getPrincipal();
        if (username != null) {
            redisTemplate.opsForValue().set("accountName",username, 60 * 20,TimeUnit.SECONDS);
        }
        return ResposeStatus.success();
    }



    /**
     * 注册
     * @param registerVM
     * @return
     */
    @PostMapping("/register")
    public ResposeStatus register(@RequestBody RegisterVM registerVM) {
        //验证是否存在相同帐号名
        AccountDTO accountDTO = accountRest.getAccount(registerVM.getMail());
        if (accountDTO != null) {
            return ResposeStatus.error("邮箱已经被使用");
        }
        String code = StringUtils.getUUID32Str();
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        hashOperations.put(code, "email", registerVM.getMail());
        hashOperations.put(code, "password", registerVM.getPassword());
        hashOperations.put(code, "phone", registerVM.getPhone());
        redisTemplate.expire(code, 24, TimeUnit.HOURS);
        //发送验证邮件
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("mail", registerVM.getMail());
        ResponseEntity<Void> a = restTemplate.postForEntity("http://AUTH-GETAWAY-NOTICE/sendRegister", map, Void.class);
        log.info("sendRegisterMail:{},body:{}", a.getStatusCode(), a.getBody());
        return ResposeStatus.success();
    }
    /**
     * 修改密码
     * @param
     * @return
     */
    @PostMapping("/reset-password/init")
    public ResposeStatus requestPasswordReset(@RequestBody UpdatePasswordVM updatePasswordVM) {
        AccountDTO accountDTO = accountRest.getAccount(updatePasswordVM.getUsername());
        if (accountDTO != null) {
            HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
            String key = StringUtils.getUUID32Str();
            hashOperations.put(key, "username", accountDTO.getUsername());
            hashOperations.put(key, "password", updatePasswordVM.getPassword());
            redisTemplate.expire(key, 24, TimeUnit.HOURS);
            accountDTO.setKey(key);
            restTemplate.postForEntity("http://AUTH-GETAWAY-NOTICE/sendPasswordResetMail", accountDTO, Void.class);
            return ResposeStatus.success();
        }
        return ResposeStatus.error("邮箱不正确","203");
    }

    /**
     * 验证注册账号邮箱
     * @param code
     * @return
     */
    @GetMapping("/activate")
    public ResponseEntity<Void> activate(@RequestParam String code) {
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        Map<String, String> entries = hashOperations.entries(code);
        if (entries == null || entries.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        String password = entries.get("password");
        try {
            entries.put("password", Encrypt.encryptRMS(password));
        } catch (Exception e) {
            e.printStackTrace();
        }
        restTemplate.postForObject("http://AUTH-GETAWAY-NOTICE/createAccount", entries, AccountDTO.class);
        return ResponseEntity.ok().build();
    }
    /**
     * 密码重置修改
     * @param
     * @return
     */
    @GetMapping("/reset-password")
   public ResponseEntity<Void>  finishPasswordReset(@RequestParam String code) {
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        Map<String, String> entries = hashOperations.entries(code);
        if (entries == null || entries.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        String password = entries.get("password");
        try {
            entries.put("password", Encrypt.encryptRMS(password));
        } catch (Exception e) {
            e.printStackTrace();
        }
        restTemplate.postForObject("http://AUTH-GETAWAY-NOTICE/updatePassword", entries, Object.class);
        return ResponseEntity.ok().build();
 }

    /**
     * 退出登录
     * @return
     */
    @PostMapping("/logOut")
    public ResposeStatus logOut() {
        redisTemplate.delete("accountName");
        return ResposeStatus.success();
    }
}

