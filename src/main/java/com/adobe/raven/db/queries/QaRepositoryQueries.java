package com.adobe.raven.db.queries;

import com.adobe.raven.dto.qa.QaItem;
import com.adobe.raven.dto.qa.QaRepository;
import com.adobe.raven.dto.qa.ReshareQaRepository;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class QaRepositoryQueries {

    @Autowired
    public MongoTemplate mongoTemplate;

    public QaRepository insert(QaRepository qaRepository) {
        QaRepository insertedQaRepository = this.mongoTemplate.save(qaRepository, "qaRepository");

        return insertedQaRepository;
    }

    public boolean update(QaRepository qaRepository) {

        boolean isUpdated = false;
        QaRepository insertedQaRepository = this.mongoTemplate.save(qaRepository);
        if (insertedQaRepository != null) {

            isUpdated = true;
        } else {

            isUpdated = false;
        }
        return isUpdated;
    }

    public Boolean deleteRenderJob(String id) {

        Query searchQuery = new Query(Criteria.where("_id").is(id));
        DeleteResult deleteResult = mongoTemplate.remove(searchQuery, "qaRepository");
        return deleteResult.getDeletedCount() > 0 ? true : false;
    }

    public QaRepository get(String id) {

        QaRepository qaRepository = mongoTemplate.findById(id, QaRepository.class);

        return qaRepository;
    }

    public List<QaRepository> getAllRenderingInfos() {

        //List<RenderingInfo> renderingInfo = this.mongoTemplate.findAll(RenderingInfo.class);

        Query query = new Query();
      //  query.with(Sort.by(Sort.Direction.DESC, "modifiedAt"));

        List<QaRepository> renderingInfos = mongoTemplate.find(query, QaRepository.class);

        return  renderingInfos;
    }

    public Boolean insertIteam(String stepId, QaItem qaItem) {

        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(stepId));
        Update update = new Update();
        update.addToSet("items", qaItem);

        try {

            UpdateResult writeResult = this.mongoTemplate.upsert(query, update, QaRepository.class);

            if (writeResult != null) {

                System.out.println("Update successful :"
                        + writeResult.toString());

            }
        } catch (DataIntegrityViolationException die) {
            System.out.println("Update failed ====>" + die.getMessage());
        }

        return true;

    }


}
