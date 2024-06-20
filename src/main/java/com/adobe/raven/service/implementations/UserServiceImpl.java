package com.adobe.raven.service.implementations;

import com.adobe.raven.Constants;
import com.adobe.raven.Utils;
import com.adobe.raven.db.queries.UserInfoQueries;
import com.adobe.raven.dto.ResponseError;
import com.adobe.raven.dto.user.UserInfo;
import com.adobe.raven.response.JobResponse;
import com.adobe.raven.service.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserInfoQueries userInfoQueries;
    @Override
    public JobResponse addUser(UserInfo userInfo) {
        JobResponse jobResponse=new JobResponse();
        UserInfo userInfos=userInfoQueries.get(userInfo.getEmailId());
        if(userInfos==null) {
            userInfo.setGuidId(Utils.createUUID());
            UserInfo usersInfo = userInfoQueries.insert(userInfo);
            jobResponse.setUserInfo(usersInfo);
            return jobResponse;
        }
        ResponseError responseError=new ResponseError();
        responseError.setErrorCode(1);
        responseError.setMessage(Constants.UserAlreadyPresent);
        jobResponse.setError(responseError);
        return jobResponse;
    }

    @Override
    public JobResponse editUser(String guid, UserInfo userInfo) {
        JobResponse jobResponse=new JobResponse();
        UserInfo info = userInfoQueries.getByGuidID(guid);
        if(info!=null && userInfo!=null){
            info.setEmailId(userInfo.getEmailId() == null? info.getEmailId() : userInfo.getEmailId());
            info.setName(userInfo.getName() == null?info.getName():userInfo.getName());
            info.setRole(userInfo.getRole() == null?info.getRole():userInfo.getRole());
            info.setRegion(userInfo.getRegion() == null?info.getRegion():userInfo.getRegion());
            info.setActive(userInfo.isActive());
            boolean check =userInfoQueries.update(info);
            jobResponse.setUseradded(check);
            return jobResponse;
        }
        jobResponse.setUseradded(false);
        return jobResponse;
    }

    @Override
    public JobResponse deleteUser(String guidId){
        JobResponse jobResponse=new JobResponse();
        UserInfo userInfo=userInfoQueries.getByGuidID(guidId);
        if(userInfo!=null){
           userInfoQueries.deleteById(userInfo.getEmailId());
            jobResponse.setMessage(Constants.UserDeleted);
            return jobResponse;
        }
        ResponseError responseError=new ResponseError();
        responseError.setErrorCode(1);
        responseError.setMessage(Constants.UserNotFound);
        jobResponse.setError(responseError);
        return jobResponse;
    }
}
