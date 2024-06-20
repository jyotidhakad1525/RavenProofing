package com.adobe.raven.request;

import com.adobe.raven.dto.geoReviewer.GeoReviewer;
import lombok.Data;

import java.util.ArrayList;

public @Data
class GeoReviewerRequest {

    private ArrayList<String> languages;
    private ArrayList<GeoReviewer> reviewers;
}
