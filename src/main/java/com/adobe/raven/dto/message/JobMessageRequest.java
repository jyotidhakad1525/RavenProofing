package com.adobe.raven.dto.message;

import lombok.Data;

public @Data
class JobMessageRequest {

    private String name;
    private String base64;
}
