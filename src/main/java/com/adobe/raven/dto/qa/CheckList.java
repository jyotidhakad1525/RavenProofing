package com.adobe.raven.dto.qa;

import lombok.Data;

import java.util.ArrayList;

public @Data class CheckList {

    private String type;

    private String name;

    private String label;

    private String state;

    private ArrayList<Link> data;

}
