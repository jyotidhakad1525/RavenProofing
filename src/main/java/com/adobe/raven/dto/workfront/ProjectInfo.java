package com.adobe.raven.dto.workfront;

import lombok.Data;

public @Data
class ProjectInfo {
    private String id;

    private String name;
    private WorkfrontOwner workfrontOwner;
    private String workfrontSponsor;
    private String plannedCompletionDate;
    private String enteredByID;
    private String enteredBy;
    private String enteredByEmail;

    private String status;
    private String description;
    private WorkfrontCustomForm workfrontCustomForm;
    private String templateID;
    private String templateValue;

    private String assignedUserId;
    private String assignedUserName;
    private String assignedUseEmail;
}
