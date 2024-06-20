package com.adobe.raven.db.queries;

import com.adobe.raven.dto.job.MasterJob;
import com.adobe.raven.dto.message.MessageRepository;
import com.mongodb.client.result.DeleteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MasterJobQueries {

    @Autowired
    public MongoTemplate mongoTemplate;

    public MasterJob insert(MasterJob masterJob) {
        MasterJob insertedMasterJob = this.mongoTemplate.save(masterJob, "masterJob");

        return insertedMasterJob;
    }

    public boolean update(MasterJob masterJob) {

        boolean isUpdated = false;
        MasterJob insertedMasterJob = this.mongoTemplate.save(masterJob);
        if (insertedMasterJob != null) {

            isUpdated = true;
        } else {

            isUpdated = false;
        }
        return isUpdated;
    }

    public Boolean deleteRenderJob(String id) {

        Query searchQuery = new Query(Criteria.where("_id").is(id));
        DeleteResult deleteResult = mongoTemplate.remove(searchQuery, "masterJob");
        return deleteResult.getDeletedCount() > 0 ? true : false;
    }

    public MasterJob get(String id) {

        MasterJob renderingInfo = mongoTemplate.findById(id, MasterJob.class);

        return renderingInfo;
    }

    public List<MasterJob> getAllMasterJobs() {

        //List<RenderingInfo> renderingInfo = this.mongoTemplate.findAll(RenderingInfo.class);

        int pageSize = 50; // default 20 records would be shown
        int pageNumber = 0; // default page 0 would be shown

        Query query = new Query();
       // query.with(Sort.by(Sort.Direction.DESC, "masterJobMetadata.lastModifiedAt"));

        // sorting
        Sort sort = null;
        Sort.Direction direction = Sort.Direction.DESC;
        sort = Sort.by(direction,"masterJobMetadata.lastModifiedAt");

        // pagination
        Pageable pageable = null;
        pageable =  PageRequest.of(pageNumber, pageSize, sort);
        query.with(pageable);

        List<MasterJob> masterJob = mongoTemplate.find(query, MasterJob.class);

        return  masterJob;
    }

    public MasterJob getByWorkfrontId(String workfrontId) {

        //hashCode
        Query query = new Query();

        Criteria criteria = new Criteria();

        criteria.and("workfrontId").is(workfrontId);

        query.addCriteria(criteria);

        List<MasterJob> contentStorageInfo = this.mongoTemplate.find(query, MasterJob.class);

        return  contentStorageInfo.size() > 0 ? contentStorageInfo.get(0) : null;
    }

//    public MasterJob getUserRenderingInfos(MasterJob renderingRequest, UserInfo userInfo) {
//
//        RenderingResponse renderingResponse = new RenderingResponse();
//
//        String userId = renderingRequest.getUserId();
//        Query query = new Query();
//
//
//        // get filtering options
//
//        Criteria criteria = new Criteria();
//        PageOptions options = renderingRequest.getPageOptions();
//
//        Sort sort = null;
//        Sort.Direction direction = Sort.Direction.DESC;
//        Pageable pageable = null;
//        int pageSize = 20; // default 20 records would be shown
//        int pageNumber = 0; // default page 0 would be shown
//        Criteria serachCriteria = null;
//
//
//        if(options != null) {
//
//            RenderingInfoOrder order = renderingRequest.getPageOptions().getOrder();
//
//
//            // for ordering
//            if(order != null
//                    && order.getSortOrder()!= null
//                    && order.getSortBy() != null) {
//
//                if(order.getSortOrder().equals("ASC")) {
//                    direction = Sort.Direction.ASC;
//                } else {
//                    direction = Sort.Direction.DESC;
//                }
//                sort = Sort.by(direction,order.getSortBy());
//            }
//
//            // for paging
//            pageNumber = options.getPageNumber();
//            pageSize = (int) options.getLimit();
//
//            // for search criteria
//            if(options.getSearchText() != null
//                    && options.getSearchIn() != null
//                    && options.getSearchIn().size() > 0) {
//                serachCriteria = new Criteria();
//                ArrayList<Criteria> searchCriterias = new ArrayList<>();
//
//                for(String option : options.getSearchIn()) {
//
//                    searchCriterias.add(Criteria.where(option).regex(options.getSearchText(), "i"));
//                }
//                serachCriteria.orOperator(searchCriterias.toArray(new Criteria[searchCriterias.size()]));
//            }
//
//            // for filters
//            if(options.getMatch() != null
//                    && options.getMatch().size() > 0) {
//
//                Set<String> keys = options.getMatch().keySet();
//                for(String key : keys) {
//
//                    criteria.and(key).is(options.getMatch().get(key));
//                }
//
//            }
//        }
//
//        if(sort == null) { // default sort
//
//            sort = Sort.by(direction,"modifiedAt");
//        }
//
//
//        //	pageable =  PageRequest.of(pageNumber, pageSize, sort);
//        //	query.with(pageable);
//        //query.with(Sort.by(Sort.Direction.DESC, "modifiedAt"));
//
//
//
//        if(userInfo.getRole().equals(UserRoles.TECHNICAL_MARKETER)) {
//            criteria.and("createdBy").is(userId);
//        }
//
//        if(serachCriteria != null) {
//            criteria.andOperator(serachCriteria);
//        }
//
//        query.addCriteria(criteria);
//        Long count = mongoTemplate.count(query, RenderingInfo.class);
//
//        renderingResponse.setTotalRenderingInfo(count);
//        pageable =  PageRequest.of(pageNumber, pageSize, sort);
//        query.with(pageable);
////		Query query = new Query(Criteria
////				.where("createdBy").is(userId));
//
//        //.and("status").is("enabled"));
//
//        List<RenderingInfo> renderingInfos = mongoTemplate.find(query, RenderingInfo.class);
//        renderingResponse.setRenderingInfos(renderingInfos);
//        return  renderingResponse;
//    }

    public List<String> getUniqueValues(String column) {
        Query query = new Query();
        List<String> uniqueColumnValues = mongoTemplate.findDistinct(query,column, MasterJob.class,String.class);
        return  uniqueColumnValues;
    }
}
