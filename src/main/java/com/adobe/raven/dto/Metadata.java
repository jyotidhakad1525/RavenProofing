package com.adobe.raven.dto;

import lombok.Data;

public @Data class Metadata {

	private String createdBy;
	private String createdById;
	private String lastModifiedBy;
	private String lastModifiedById;
	private long createdAt;
	private long lastModifiedAt;
	
}
