package com.adobe.raven.dto.qa;

import lombok.Data;

import java.util.ArrayList;

public @Data class ReshareQaRepository {


	private String id; // ref from message id
	private String status;
	private String bu;
	private String segment;
	private ArrayList<CheckList> checkList;
}
