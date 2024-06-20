package com.adobe.raven.dto.geoReviewer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;

@JsonInclude(JsonInclude.Include.NON_NULL)
public @Data
class GeoReviewer {

    @Id
    private String id;

   // @JsonProperty(value = "Reviewer Email")
    private String email;

   // @JsonProperty(value = "Reviewer Name")
    private String name;

    //@JsonProperty(value = "Language Code")
    private String language;

    private String status;

    //@JsonProperty(value = "Reviewer Type")
    private String geoReviewerType; // primary or secondary
}
