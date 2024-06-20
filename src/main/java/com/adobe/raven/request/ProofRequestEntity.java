package com.adobe.raven.request;

import lombok.Data;

import java.util.ArrayList;

@Data
public class ProofRequestEntity {

    String from;
    String to;
    String cc;
    String subject;
    String message;
    ArrayList<String> ids;
}
