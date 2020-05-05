package fr.lelouet.application.settings.examples.types;

import java.util.ArrayList;

public class Node {
    private ArrayList<Node> children = new ArrayList<>();
    private String value = new String();

    public ArrayList<Node> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<Node> value) {
        children = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        value = value;
    }
}
