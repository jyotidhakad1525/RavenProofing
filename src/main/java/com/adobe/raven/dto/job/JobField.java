package com.adobe.raven.dto.job;

import lombok.Data;

public @Data class JobField {

	private String id;
	private String key;
	private String label;
	private String dataType;
	private boolean filter; 
	private int position; 
	private boolean visible; 
	
}
