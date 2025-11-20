package com.example.SINJA.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class Node {
    ArrayList<Tuple> data;
    ArrayList<Node> links;
    Node father;
    Node sheetLinksNext;
    Node sheetLinksBack;
    int order;


    public Node() {
        this.order = 2;
        data = new ArrayList<>(2 * order);
        links = new ArrayList<>(2 * order - 1);
        sheetLinksNext = null;
        sheetLinksBack = null;
        father = null;
    }

}
