package com.adobe.raven.dto.proof;

import com.adobe.raven.dto.message.ProofMessageInfo;
import lombok.Data;

import java.util.ArrayList;

public @Data
class ProofInfo {

    private String language;
    private String languageLabel;
    private String type;
    private ArrayList<ProofMessageInfo> messageDetails;
    private int numberOfProofs;
    private int size;
    private String status;
    private boolean geoReviewerApproved;
    private boolean pmApproved;

}
