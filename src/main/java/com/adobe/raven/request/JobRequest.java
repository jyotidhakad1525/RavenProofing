package com.adobe.raven.request;

import com.adobe.raven.dto.cgen.CgenContent;
import com.adobe.raven.dto.job.JobSegmentInfo;
import com.adobe.raven.dto.message.JobMessageRequest;
import com.adobe.raven.dto.user.UserInfo;
import com.adobe.raven.dto.workfront.WorkfrontRepository;
import lombok.Data;

import java.util.ArrayList;

public @Data
class JobRequest {

    private String userId;
    private WorkfrontRepository workfrontInfo;
    private ArrayList<CgenContent> cgen;
    private ArrayList<JobMessageRequest> messages;
    private ArrayList<UserInfo> qas;
    private ArrayList<JobSegmentInfo> segmentInfos;
    private String jobType;

    private int type; // 1 - update msg files for QA-1, QA-2 etc.
    private String qaId;

    private String qaStatus;

    private String base64;
}
