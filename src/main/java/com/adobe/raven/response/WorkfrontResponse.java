package com.adobe.raven.response;


import com.adobe.raven.dto.ResponseError;
import com.adobe.raven.dto.workfront.ProjectInfo;
import lombok.Data;

public @Data
class WorkfrontResponse {

	private ResponseError error;
	private ProjectInfo projectInfo;


}
