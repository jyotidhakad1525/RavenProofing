package com.adobe.raven.dto.cgen;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

public @Data
class CgenContent {

    @JsonProperty(value = "program id")
    private String programId;

    @JsonProperty(value = "program name")
    private String programName;

    @JsonProperty(value = "activity id")
    private String activityId;

    @JsonProperty(value = "activity name")
    private String activityName;

    @JsonProperty(value = "media type")
    private String mediaType;

    @JsonProperty(value = "channel")
    private String channel;

    @JsonProperty(value="tag id/code")
    private String tagId;

    @JsonProperty(value = "tag name")
    private String tagName;

    @JsonProperty(value = "tag URL")
    private String tagUrl;

    @JsonProperty(value = "tag full url")
    private String tagFullUrl;

    @JsonProperty(value = "creative file name")
    private String creativeFileName;

    @JsonProperty(value = "subj line")
    private String subjectLine;

    @JsonProperty(value = "preheader")
    private String preHeader;


}
