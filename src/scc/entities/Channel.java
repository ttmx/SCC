package scc.entities;

import org.bson.Document;

import java.util.Arrays;
import java.util.List;

public class Channel {

    private String id;
    private String name;
    private String owner;
    private boolean publicChannel;
    private String[] members;

    public Channel() {
    }

    public Channel(String id, String name, String owner, boolean publicChannel, String[] members) {
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

    public String[] getMembers() { return this.members; }

    public void setMembers(String[] members) {
        this.members = members;
    }

    public boolean getPublicChannel() {
        return publicChannel;
    }

    public void setPublicChannel(boolean publicChannel) {
        this.publicChannel = publicChannel;
    }

    @Override
    public String toString() {
        return "Channel [id=" + this.id + ", name=" + this.name + ", owner=" + this.owner + ", publicChannel=" + this.publicChannel + ", members=" + Arrays.toString(this.members) + "]";
    }

    static public Channel fromDocument(Document doc) {
        return new Channel (
                (String)doc.get("_id"),
                (String)doc.get("name"),
                (String)doc.get("owner"),
                (boolean)doc.get("publicChannel"),
                (String[]) ((List<String>)doc.get("members")).toArray()
        );
    }

    public Document toDocument() {
        return new Document("_id", id)
                .append("name", name)
                .append("owner", owner)
                .append("publicChannel", publicChannel)
                .append("members", Arrays.asList(members));
    }

}
