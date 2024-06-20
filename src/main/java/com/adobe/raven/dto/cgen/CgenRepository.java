package com.adobe.raven.dto.cgen;

import com.adobe.raven.dto.Metadata;
import lombok.Data;

import java.util.ArrayList;

public @Data
class CgenRepository {

    private String id;
    private ArrayList<CgenContent> content;
    private String md5;
    private String type;
    private Metadata metadata;
}
