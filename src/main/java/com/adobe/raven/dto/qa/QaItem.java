package com.adobe.raven.dto.qa;



import com.adobe.raven.dto.Metadata;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;

public @Data class QaItem {

	@Id
	private String id; // ref from message id
	private String status;
	private String segment;
	private String bu;
	private Metadata metadata;
	private ArrayList<CheckList> checkList;
}
