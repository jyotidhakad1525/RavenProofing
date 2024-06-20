package com.adobe.raven.db.queries;

import com.adobe.raven.dto.LanguageCodeRepository;
import com.adobe.raven.dto.workfront.WorkfrontRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LanguageRepositoryQueries {

    @Autowired
    public MongoTemplate mongoTemplate;

    public LanguageCodeRepository get(String id) {

        LanguageCodeRepository languageCodeRepository = mongoTemplate.findById(id,
                LanguageCodeRepository.class);
        return languageCodeRepository;
    }
}
