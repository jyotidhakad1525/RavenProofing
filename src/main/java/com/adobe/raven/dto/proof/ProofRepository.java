package com.adobe.raven.dto.proof;

import com.adobe.raven.dto.Metadata;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;

public @Data
class ProofRepository {

    @Id
    private String id;
    private String cgenId;
    private String status;
    private ArrayList<ProofItem> items;
    private Metadata metadata;
}
