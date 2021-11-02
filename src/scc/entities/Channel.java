package scc.entities;

import java.util.List;

public class Channel {

    private String id;
    private String name;
    private String owner;
    private boolean publicChannel;
    private List<Integer> members;

    public Channel() {
    }

    public Channel(String id, String name, String owner, boolean publicChannel, List<Integer> members) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.publicChannel = publicChannel;
        this.members = members;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() { return this.owner; }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<Integer> getMembers() { return this.members; }

    public void setMembers(List<Integer> members) {
        this.members = members;
    }

    public boolean getPublicChannel() {
        return publicChannel;
    }

    public void setPublicChannel(boolean publicChannel) {
        this.publicChannel = publicChannel;
    }

}
