package com.adobe.raven.response;

import com.adobe.raven.dto.ResponseError;
import com.adobe.raven.dto.message.MessageRepository;
import com.adobe.raven.dto.proof.ProofInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
public @Data
class ProofResponse {

    private ResponseError error;
    private ArrayList<ProofInfo> result;
    private ArrayList<MessageRepository> messages;
    private Boolean proofSent;
    private Boolean geoReviewerApproved;
    private Boolean pmApproved;

}
