package com.adobe.raven.dto.proof;

import lombok.Data;
import org.springframework.data.annotation.Id;

public @Data class ProofItem {

    @Id
    private String id;

    private String status;
    private String bu;
    private String language;
    private String segment;

    private boolean isGeoReviewerApproved;
    private boolean isPmReviewerReviewed;
}
