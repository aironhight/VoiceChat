package sep.voicechat.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Channel implements Serializable {
    private String owner;
    private ArrayList<String> users;
    private String name;

    public Channel() {

    }

    public Channel(String owner, String name) {
        this.owner = owner;
        users = new ArrayList<>();
        users.add(owner);
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public String getName() {
        return name;
    }
}
