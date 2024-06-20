package com.adobe.raven.db.queries;

import com.adobe.raven.dto.proof.ProofRepository;
import com.adobe.raven.dto.qa.QaRepository;
import com.mongodb.client.result.DeleteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProofRepositoryQueries {

    @Autowired
    public MongoTemplate mongoTemplate;

    public ProofRepository insert(ProofRepository proofRepository) {
        ProofRepository insertedProofRepository = this.mongoTemplate.save(proofRepository,
                "proofRepository");

        return insertedProofRepository;
    }

    public boolean update(ProofRepository proofRepository) {

        boolean isUpdated = false;
        ProofRepository insertedProofRepository = this.mongoTemplate.save(proofRepository);
        if (insertedProofRepository != null) {

            isUpdated = true;
        } else {

            isUpdated = false;
        }
        return isUpdated;
    }

    public Boolean deleteRenderJob(String id) {

        Query searchQuery = new Query(Criteria.where("_id").is(id));
        DeleteResult deleteResult = mongoTemplate.remove(searchQuery, "proofRepository");
        return deleteResult.getDeletedCount() > 0 ? true : false;
    }

    public ProofRepository get(String id) {

        ProofRepository proofRepository = mongoTemplate.findById(id, ProofRepository.class);

        return proofRepository;
    }

    public List<ProofRepository> getAllRenderingInfos() {

        //List<RenderingInfo> renderingInfo = this.mongoTemplate.findAll(RenderingInfo.class);

        Query query = new Query();
      //  query.with(Sort.by(Sort.Direction.DESC, "modifiedAt"));

        List<ProofRepository> proofRepositories = mongoTemplate.find(query, ProofRepository.class);

        return  proofRepositories;
    }




}
