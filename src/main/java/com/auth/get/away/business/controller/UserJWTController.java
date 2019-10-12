package com.auth.get.away.business.controller;

import com.auth.get.away.business.controller.vm.RegisterVM;
import com.auth.get.away.business.rest.dto.AccountDTO;
import com.auth.get.away.business.rest.service.IAccountRest;
import com.auth.get.away.business.security.jwt.TokenProvider;
import com.boostor.framework.rest.ResposeStatus;
import com.boostor.framework.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import javax.validation.Valid;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.HashOperations;

/**
 * Controller to authenticate users.
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class UserJWTController {

    private final TokenProvider tokenProvider;

    private final AuthenticationManager authenticationManager;

    private final RestTemplate restTemplate;

    private final StringRedisTemplate redisTemplate;

    private final PasswordEncoder passwordEncoder;

    private final IAccountRest accountRest;

    public UserJWTController(TokenProvider tokenProvider,
                             AuthenticationManager authenticationManager,
                             PasswordEncoder passwordEncoder,
                             RestTemplate restTemplate,
                             IAccountRest accountRest,StringRedisTemplate redisTemplate) {
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.accountRest = accountRest;
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/user")
    public ResponseEntity<AccountDTO> user(Principal principal) {
        Authentication authentication = (Authentication) principal;
        AccountDTO accountDTO = accountRest.getAccount(authentication.getName());
        accountDTO.setPassword("");
        return ResponseEntity.ok(accountDTO);
    }

//    @PostMapping("/authenticate")
//    public ResponseEntity<JWTToken> authorize(@Valid @RequestBody LoginVM loginVM) {
////登录记录TODO
//        UsernamePasswordAuthenticationToken authenticationToken =
//                new UsernamePasswordAuthenticationToken(loginVM.getUsername(), loginVM.getPassword());
//        AbstractAuthenticationToken authentication = (AbstractAuthenticationToken) this.authenticationManager.authenticate(authenticationToken);
//        AccountDTO accountDTO = accountRest.getAccount(loginVM.getUsername());
//        Map<String, Object> detail = new HashMap<>();
//        detail.put("storeId", accountDTO.getStoreId());
//        authentication.setDetails(detail);
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        boolean rememberMe = (loginVM.isRememberMe() == null) ? false : loginVM.isRememberMe();
//        String jwt = tokenProvider.createToken(authentication, rememberMe);
//        System.out.println(jwt);
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
//        return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.OK);
//    }

    /**
     * confirm: "12345678"
     * mail: "wench@boostor.com"
     * password: "12345678"
     *
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
        hashOperations.put(code, "storeName", registerVM.getStoreName());
        hashOperations.put(code, "email", registerVM.getMail());
        hashOperations.put(code, "password", registerVM.getPassword());
        redisTemplate.expire(code, 24, TimeUnit.HOURS);
        //发送验证邮件
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("mail", registerVM.getMail());
        //TODO 考虑使用动态路由获取
        ResponseEntity<Void> a = restTemplate.postForEntity("http://AUTH-GETAWAY-NOTICE/sendRegister", map, Void.class);
        log.info("sendRegisterMail:{},body:{}", a.getStatusCode(), a.getBody());
        return ResposeStatus.success();
    }

    /**
     * 验证邮箱
     *
     * @param code
     * @return
     */
    @GetMapping("/activate")
    public ResponseEntity<Void> register(@RequestParam String code) {
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        Map<String, String> entries = hashOperations.entries(code);
        if (entries == null || entries.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        String password = entries.get("password");
        entries.put("password", passwordEncoder.encode(password));
        AccountDTO createRespStr = restTemplate.postForObject("http://SHOPLALA-SERVICE-BUSINESS/createAccount", entries, AccountDTO.class);
        entries.put("storeId", createRespStr.getStoreId());
        //初始化政策、主题、通知
//        restTemplate.postForEntity("http://SHOPLALA-SERVICE-BUSINESS/initLegal", entries, Void.class);
//        restTemplate.postForEntity("http://SHOPLALA-SERVICE-THEME/initThemeData", entries, Void.class);
//        restTemplate.postForEntity("http://SHOPLALA-SERVICE-NOTICE/initNotice", entries, Void.class);
        return ResponseEntity.ok().build();
    }

    /**
     * 验证邮箱
     *
     * @param
     * @return
     */
//    @PostMapping("/reset-password/init")
//    public ResposeStatus requestPasswordReset(@RequestBody String mail) {
//        AccountDTO accountDTO = accountRest.getAccount(mail);
//        if (accountDTO != null) {
//            HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
//            String key = StringUtils.getUUID32Str();
//            hashOperations.put(key, "username", accountDTO.getUsername());
//            redisTemplate.expire(key, 24, TimeUnit.HOURS);
//            accountDTO.setKey(key);
//            restTemplate.postForEntity("http://SHOPLALA-SERVICE-NOTICE/sendPasswordResetMail", accountDTO, Void.class);
//            return ResposeStatus.success();
//        }
//        return ResposeStatus.error("邮箱不正确");
//    }

    /**
     * 密码重置修改
     *
     * @param
     * @return
     */
//    @PostMapping("/reset-password/finish")
//    public ResposeStatus finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) {
//        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
//        String username = hashOperations.get(keyAndPassword.getKey(), "username");
//        Map<String, Object> map = new HashMap<>();
//        map.put("username", username);
//        map.put("password", passwordEncoder.encode(keyAndPassword.getPassword()));
//        ResposeStatus resposeStatus = restTemplate.postForObject("http://SHOPLALA-SERVICE-BUSINESS/updatePassword", map, ResposeStatus.class);
//        if (resposeStatus != null && resposeStatus.getStatus()) {
//            redisTemplate.delete(keyAndPassword.getKey());
//        }
//        return resposeStatus;
//    }

    /**
     * 密码重置修改
     *
     * @param
     * @return
     */
//    @PostMapping("/update-password")
//    public ResposeStatus finishPasswordReset(@RequestBody UpdatePasswordVM updatePasswordVM) {
//
//        updatePasswordVM.setPassword(passwordEncoder.encode(updatePasswordVM.getPassword()));
//        return restTemplate.postForObject("http://SHOPLALA-SERVICE-BUSINESS/updatePassword", updatePasswordVM, ResposeStatus.class);
//
//    }

    /**
     * Object to return as body in JWT Authentication.
     */
    static class JWTToken {

        private String idToken;
        private String storeId;


        JWTToken(String idToken) {
            this.idToken = idToken;
            this.storeId = "storeIdc6a9be827016a9bfac61f0009";
        }
        @JsonProperty("id_token")
        String getIdToken() {
            return idToken;
        }
        void setIdToken(String idToken) {
            this.idToken = idToken;
        }
        public String getStoreId() {
            return storeId;
        }
        public void setStoreId(String storeId) {
            this.storeId = storeId;
        }
    }
}
