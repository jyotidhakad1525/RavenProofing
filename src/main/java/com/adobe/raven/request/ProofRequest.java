package com.adobe.raven.request;

import com.adobe.raven.dto.proof.ProofRequestReviewers;
import lombok.Data;

import java.util.ArrayList;

public @Data
class ProofRequest {

    private String userId;
    private ArrayList<ProofRequestReviewers> selectedGeoReviewers;


    private String guidId;
    private String from;
    private ArrayList<String> to;
    private ArrayList<String> cc;
    private String subject;
    private String messages;
    private ArrayList<String> messagesIds;

}
