package com.adobe.raven.dto.message;

import com.adobe.raven.dto.HtmlInfo;
import com.adobe.raven.dto.Metadata;
import lombok.Data;
import org.springframework.data.annotation.Id;

public @Data
class MessageRepository {

    @Id
    private String id;
    private String name;
    private String md5;
    private String locale;
    private String language;
    private String activityId;
    private String creativeFileName;
    private String senderAddress;
    private String senderName;
    private String subject;
    private HtmlInfo html;
    private Metadata metadata;
    private MessageContent content;
    private String status;

}
