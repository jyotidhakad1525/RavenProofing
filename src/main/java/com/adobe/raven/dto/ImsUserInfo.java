package com.adobe.raven.dto;

import lombok.Data;

public @Data
class ImsUserInfo {

    private String clientId;
    private String bearerToken;
    private String type;
    private String url;
}
