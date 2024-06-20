package com.adobe.raven.db.queries;

import com.adobe.raven.dto.qa.CheckList;
import com.adobe.raven.dto.qa.QaItem;
import com.adobe.raven.dto.qa.ReshareQaRepository;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class ReshareQAInfoRepository {

	@Autowired
	public MongoTemplate mongoTemplate;

	public ReshareQaRepository insert(ReshareQaRepository qaInfoRepository) {
		ReshareQaRepository qaInfo = this.mongoTemplate.save(qaInfoRepository, "reshareQaRepository");
	
		return qaInfo;
	}

	public ReshareQaRepository get(String id) {

		ReshareQaRepository qaInfo = mongoTemplate.findById(id, ReshareQaRepository.class);

		return qaInfo;
	}



		public Boolean updateCheckList(String stepId, String messageId, CheckList checkList) {

			Query query = new Query();
			query.addCriteria(Criteria.where("_id").is(messageId)
					);
			Update update = new Update();
			update.addToSet("checkList", checkList);

			try {

				UpdateResult writeResult = this.mongoTemplate.upsert(query, update, ReshareQaRepository.class);

				if (writeResult != null) {

					System.out.println("Update successful :"
							+ writeResult.toString());

				}
			} catch (DataIntegrityViolationException die) {
				System.out.println("Update failed ====>" + die.getMessage());
			}

			return true;

		}

	public Boolean updateStatus(String stepId, String messageId, String status) {

		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(messageId));

		Update update = new Update();
		update.set("status", status);

		try {

			UpdateResult writeResult = this.mongoTemplate.upsert(query, update, ReshareQaRepository.class);

			if (writeResult != null) {

				System.out.println("Update successful :"
						+ writeResult.toString());

			}
		} catch (DataIntegrityViolationException die) {
			System.out.println("Update failed ====>" + die.getMessage());
		}

		return true;

	}

	public ReshareQaRepository getListByField(String field, String value) {
		Query query = new Query();
		Criteria criteria = new Criteria();

		criteria.and(field).is(value);
		query.addCriteria(criteria);
		List<ReshareQaRepository> contentInfos = mongoTemplate.find(query, ReshareQaRepository.class);

		if(contentInfos != null
			&& contentInfos.size() > 0) {
			return contentInfos.get(0);
		}

		return null;
	}



}
