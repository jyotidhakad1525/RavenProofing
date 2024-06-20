package com.adobe.raven.response;

import com.adobe.raven.dto.ResponseError;
import com.adobe.raven.dto.user.UserInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public @Data
class UserResponse {

    private ResponseError error;
    private List<UserInfo> results;
    private UserInfo userInfo;
}
