package com.adobe.raven.db.queries;

import com.adobe.raven.dto.user.UserInfo;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserInfoQueries {

    @Autowired
    public MongoTemplate mongoTemplate;

    public UserInfo insert(UserInfo userInfo) {
        UserInfo insertedUserInfo = this.mongoTemplate.save(userInfo, "userInfo");

        return insertedUserInfo;
    }



    public boolean update(UserInfo userInfo) {

        boolean isUpdated = false;
        UserInfo insertedUserInfo = this.mongoTemplate.save(userInfo);
        if (insertedUserInfo != null) {

            isUpdated = true;
        } else {

            isUpdated = false;
        }
        return isUpdated;
    }

    public UserInfo get(String id) {

        UserInfo userInfo = mongoTemplate.findById(id, UserInfo.class);

        return userInfo;
    }

    public List<UserInfo> getCampaignDevelopers() {


        return generalGetList("role", "CampaignDeveloper");
    }

    public List<UserInfo> getAllList() {

        return mongoTemplate.findAll(UserInfo.class);
    }

    public UserInfo getByGuidID(String guidId) {

        List<UserInfo> users = generalGetList("guidId", guidId);
        UserInfo user = null;

        if(users != null && users.size() > 0) {
            user = users.get(0);
        }
        return user;
    }

    public List<UserInfo> generalGetList(String key, String value) {
        Query query = new Query();

        Criteria criteria = new Criteria();
        criteria.and(key).is(value);

        query.addCriteria(criteria);
        List<UserInfo> users = mongoTemplate.find(query, UserInfo.class);

        return users;
    }
    public UserInfo findByEmail(String id) {

        UserInfo userInfo = mongoTemplate.findById(id, UserInfo.class);

        return userInfo;
    }

    public  void  deleteById(String id){

        Query query = new Query();

        Criteria criteria = new Criteria();
        criteria.and("_id").is(id);
        query.addCriteria(criteria);
        mongoTemplate.findAndRemove(query,UserInfo.class);
    }
}
