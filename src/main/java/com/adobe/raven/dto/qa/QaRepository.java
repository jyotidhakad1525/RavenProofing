package com.adobe.raven.dto.qa;

import java.util.ArrayList;

import com.adobe.raven.dto.Metadata;
import lombok.Data;
import org.springframework.data.annotation.Id;

public @Data class QaRepository {

	@Id
	private String id;
	private String assignee;
	private String cgenId;
	private ArrayList<QaItem> items;
	private Metadata metadata;
	
}
