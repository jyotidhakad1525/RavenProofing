package com.adobe.raven.dto.workfront;

import lombok.Data;
import org.springframework.data.annotation.Id;

public @Data
class WorkfrontRepository {

    @Id
    private String id;
    private String name;
    private String developer;
    private String programManager;
    private String projectSponsor;
    private String deploymentDate;
    private String cgenProgramId;
    private String status;
}
