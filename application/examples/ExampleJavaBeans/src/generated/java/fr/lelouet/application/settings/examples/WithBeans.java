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

    /**
     * @return
     *     the {@link #yes}
     */
    public boolean getYes() {
        return yes;
    }

    /**
     * set the {@link #yes}
     */
    public void setYes(boolean value) {
        this.yes = value;
    }

    /**
     * @return
     *     the {@link #name}
     */
    public String getName() {
        return name;
    }

    /**
     * set the {@link #name}
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * @return
     *     the {@link #locations}
     */
    public Locations getLocations() {
        return locations;
    }

    /**
     * set the {@link #locations}
     */
    public void setLocations(Locations value) {
        this.locations = value;
    }

    /**
     * @return
     *     the {@link #root}
     */
    public Node getRoot() {
        return root;
    }

    /**
     * set the {@link #root}
     */
    public void setRoot(Node value) {
        this.root = value;
    }

    /**
     * @return
     *     the {@link #people}
     */
    public LinkedHashMap<String, String> getPeople() {
        return people;
    }

    /**
     * set the {@link #people}
     */
    public void setPeople(LinkedHashMap<String, String> value) {
        this.people = value;
    }
}
