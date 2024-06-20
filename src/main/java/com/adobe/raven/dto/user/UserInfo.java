package com.adobe.raven.dto.user;

import lombok.Data;
import org.springframework.data.annotation.Id;

public @Data
class UserInfo {

    @Id
    private String emailId;
    private String guidId;
    private String name;
    private String role; // Admin, PM, Campaign Developer
    private String region;
    boolean active;
}
