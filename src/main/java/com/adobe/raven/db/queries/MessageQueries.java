package com.adobe.raven.db.queries;

import com.adobe.raven.dto.message.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MessageQueries {

    @Autowired
    public MongoTemplate mongoTemplate;

    public MessageRepository insert(MessageRepository contentInfo) {
        MessageRepository insertedMessageRepository = this.mongoTemplate.save(contentInfo, "messageRepository");

        return insertedMessageRepository;
    }



    public boolean update(MessageRepository messageRepository) {

        boolean isUpdated = false;
        MessageRepository insertedContentStorageInfo = this.mongoTemplate.save(messageRepository);
        if (insertedContentStorageInfo != null) {

            isUpdated = true;
        } else {

            isUpdated = false;
        }
        return isUpdated;
    }

    public MessageRepository get(String id) {

        MessageRepository messageRepository = mongoTemplate.findById(id, MessageRepository.class);

        return messageRepository;
    }

    public void remove(MessageRepository messageRepository) {
        this.mongoTemplate.remove(messageRepository);
    }

    public void removeAll() {
        this.mongoTemplate.remove(new Query(), "messageRepository");
    }

    public MessageRepository getByHash(String hash) {

        //hashCode
        Query query = new Query();

        Criteria criteria = new Criteria();

        criteria.and("md5").is(hash);

        query.addCriteria(criteria);

        List<MessageRepository> contentStorageInfo = this.mongoTemplate.find(query, MessageRepository.class);

        return  contentStorageInfo.size() > 0 ? contentStorageInfo.get(0) : null;
    }
}
