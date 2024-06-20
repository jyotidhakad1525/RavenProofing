package com.adobe.raven.dto.job;

import java.util.ArrayList;


import com.adobe.raven.dto.Metadata;
import com.adobe.raven.dto.workfront.WorkfrontRepository;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Id;

@JsonInclude(JsonInclude.Include.NON_NULL)
public @Data class MasterJob {

	@Id
	private String id;
	private String name;
	private String workfrontId;
	private WorkfrontRepository workfrontInfo;
	private ArrayList<JobStep> steps;
	private Metadata masterJobMetadata;
	private String state;
	private String region;
	private String proofingDeadline;
	private String sharedFileLinks;
	private ArrayList<JobSegmentInfo> segmentInfos;
	
}
