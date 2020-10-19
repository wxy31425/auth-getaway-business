package com.auth.get.away.business.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JWTTokenDTO {

    private String idToken;
    private String storeId;
    public JWTTokenDTO(String idToken) {
        this.idToken = idToken;
//            this.storeId = "storeIdc6a9be827016a9bfac61f0009";
    }
    @JsonProperty("id_token")
    String getIdToken() {
        return idToken;
    }
    void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}
