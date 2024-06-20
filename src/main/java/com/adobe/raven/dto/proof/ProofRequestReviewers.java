package com.adobe.raven.dto.proof;

import lombok.Data;

import java.util.ArrayList;

public @Data
class ProofRequestReviewers {

    private String language;
    private Boolean urgent;
    private ArrayList<String> sendList;
}
