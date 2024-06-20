package com.adobe.raven.dto.user;

import lombok.Data;

public @Data
class ImsUserDetails {

    private String account_type;
    private String utcOffset;
    private String displayName;
    private String last_name;
    private String userId;
    private String authId;
    private  String countryCode;
    private String first_name;
    private String email;

}
