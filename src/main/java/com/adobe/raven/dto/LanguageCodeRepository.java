package com.adobe.raven.dto;


import lombok.Data;
import org.springframework.data.annotation.Id;

public @Data
class LanguageCodeRepository {

    @Id
    private String _id;
    private String languageName;
    private String languageCode3;
}
