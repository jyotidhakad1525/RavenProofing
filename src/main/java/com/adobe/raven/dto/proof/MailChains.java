package com.adobe.raven.dto.proof;

import java.util.ArrayList;

import com.adobe.raven.dto.message.MessageRepository;
import lombok.Data;
import org.springframework.data.annotation.Id;

public @Data
class MailChains {

	private String id; 
	
	
	private String toList;
	private String ccList;
	private String language;
	private String body;
	private String subject;
	private String isUrgent;
	private ArrayList<MessageRepository> attachments;

	
}
