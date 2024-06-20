package com.adobe.raven.dto.proof;

import lombok.Data;

public @Data
class ProofUpdateRequest {

    private String languageId;
    private int type;
    private boolean isGeoReviewerApproved;
    private boolean isPmApproved;
}
