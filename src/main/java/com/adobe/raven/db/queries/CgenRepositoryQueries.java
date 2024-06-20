package com.adobe.raven.db.queries;

import com.adobe.raven.dto.cgen.CgenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CgenRepositoryQueries {

    @Autowired
    public MongoTemplate mongoTemplate;

    public CgenRepository insert(CgenRepository cgenRepository) {
        CgenRepository insertedCgenRepository = this.mongoTemplate.save(cgenRepository, "cgenRepository");

        return insertedCgenRepository;
    }



    public boolean update(CgenRepository cgenRepository) {

        boolean isUpdated = false;
        CgenRepository insertedCgenRepository = this.mongoTemplate.save(cgenRepository);
        if (insertedCgenRepository != null) {

            isUpdated = true;
        } else {

            isUpdated = false;
        }
        return isUpdated;
    }

    public CgenRepository get(String id) {

        CgenRepository contentInfo = mongoTemplate.findById(id, CgenRepository.class);

        return contentInfo;
    }

    public void remove(CgenRepository contentInfo) {
        this.mongoTemplate.remove(contentInfo);
    }

    public void removeAll() {
        this.mongoTemplate.remove(new Query(), "contentStorageInfo");
    }

    public CgenRepository getByHash(String hash) {

        //hashCode
        Query query = new Query();

        Criteria criteria = new Criteria();

        criteria.and("md5").is(hash);

        query.addCriteria(criteria);

        List<CgenRepository> cgenRepositories = this.mongoTemplate.find(query, CgenRepository.class);

        return  cgenRepositories.size() > 0 ? cgenRepositories.get(0) : null;
    }
}
