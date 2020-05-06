package fr.lelouet.application.settings.examples.types;

import java.util.ArrayList;

public class Node {
    private ArrayList<Node> children = new ArrayList<>();
    /**
     * description of the "value" field
     */
    private String value = new String();

    /**
     * @return
     *     the {@link #children}
     */
    public ArrayList<Node> getChildren() {
        return children;
    }

    /**
     * set the {@link #children}
     */
    public void setChildren(ArrayList<Node> value) {
        this.children = value;
    }

    /**
     * @return
     *     the {@link #value}
     */
    public String getValue() {
        return value;
    }

    /**
     * set the {@link #value}
     */
    public void setValue(String value) {
        this.value = value;
    }
}
