package com.adobe.raven.service.interfaces;

import com.adobe.raven.dto.user.UserInfo;
import com.adobe.raven.response.JobResponse;

public interface UserService {

    public JobResponse addUser(UserInfo userInfo);

    JobResponse editUser(String guid, UserInfo userInfo);

    JobResponse deleteUser(String guidId);
}
