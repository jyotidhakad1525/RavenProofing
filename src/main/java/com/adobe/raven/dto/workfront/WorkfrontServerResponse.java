package com.adobe.raven.dto.workfront;


import com.adobe.raven.dto.ResponseError;
import lombok.Data;

public @Data
class WorkfrontServerResponse {

	private ResponseError error;
	private WorkfrontResponseData data;

	public WorkfrontResponseData getData() {
		return data;
	}

	public void setData(WorkfrontResponseData data) {
		this.data = data;
	}
	
	
	
}
