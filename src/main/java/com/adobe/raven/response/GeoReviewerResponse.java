package com.adobe.raven.response;

import com.adobe.raven.dto.geoReviewer.GeoReviewer;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
public @Data
class GeoReviewerResponse {

    private ArrayList<GeoReviewer> result;
    private Boolean isInserted;
}
