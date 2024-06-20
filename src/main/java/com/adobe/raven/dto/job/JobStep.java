package com.adobe.raven.dto.job;

import com.adobe.raven.dto.cgen.CgenRepository;
import com.adobe.raven.dto.user.UserInfo;
import lombok.Data;

import java.util.List;

public @Data class JobStep {

	private String id;
	private int sequence;
	private String name;
	private String type;
	private int required;
	private String state;
	private UserInfo userInfo;
	private CgenRepository cgenInfo;
	private List<JobSegmentInfo> segmentInfoList;

	
}
