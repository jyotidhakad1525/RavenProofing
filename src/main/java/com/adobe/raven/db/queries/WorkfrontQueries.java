package com.adobe.raven.db.queries;

import com.adobe.raven.dto.workfront.WorkfrontRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class WorkfrontQueries {

    @Autowired
    public MongoTemplate mongoTemplate;

    public WorkfrontRepository insert(WorkfrontRepository workfrontRepository) {
        WorkfrontRepository insertedWorkfrontRepository = this.mongoTemplate.save(workfrontRepository,
                "workfrontRepository");

        return insertedWorkfrontRepository;
    }

    public boolean update(WorkfrontRepository workfrontRepository) {

        boolean isUpdated = false;
        WorkfrontRepository insertedWorkfrontRepository = this.mongoTemplate.save(workfrontRepository);
        if (insertedWorkfrontRepository != null) {

            isUpdated = true;
        } else {

            isUpdated = false;
        }
        return isUpdated;
    }



    public WorkfrontRepository get(String id) {

        WorkfrontRepository renderingInfo = mongoTemplate.findById(id, WorkfrontRepository.class);

        return renderingInfo;
    }

    public List<WorkfrontRepository> getAllRenderingInfos() {

        //List<RenderingInfo> renderingInfo = this.mongoTemplate.findAll(RenderingInfo.class);

        Query query = new Query();
       // query.with(Sort.by(Sort.Direction.DESC, "modifiedAt"));

        List<WorkfrontRepository> workfrontRepository = mongoTemplate.find(query, WorkfrontRepository.class);

        return  workfrontRepository;
    }


}
