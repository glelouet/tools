package fr.lelouet.application.settings.examples;

import java.util.LinkedHashMap;
import fr.lelouet.application.settings.examples.types.Node;
import fr.lelouet.application.settings.examples.withbeans.Locations;

public class WithBeans {
    private boolean yes;
    private String name = new String();
    private Locations locations = new Locations();
    private Node root = new Node();
    private LinkedHashMap<String, String> people = new LinkedHashMap<>();

    public boolean getYes() {
        return yes;
    }

    public void setYes(boolean value) {
        yes = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public Locations getLocations() {
        return locations;
    }

    public void setLocations(Locations value) {
        locations = value;
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node value) {
        root = value;
    }

    public LinkedHashMap<String, String> getPeople() {
        return people;
    }

    public void setPeople(LinkedHashMap<String, String> value) {
        people = value;
    }
}
