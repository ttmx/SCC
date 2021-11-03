package scc.entities;

import org.bson.Document;

import java.util.Arrays;
import java.util.List;

public class User {
    private String id;
    private String name;
    private String pwd;
    private String photoId;
    private String[] channelIds;

    public User() {
    }

    public User(String id, String name, String pwd, String photoId, String[] channelIds) {
        super();
        this.id = id;
        this.name = name;
        this.pwd = pwd;
        this.photoId = photoId;
        this.channelIds = channelIds;
    }

    static public User fromDocument(Document doc) {
        return new User(
                (String)doc.get("_id"),
                (String)doc.get("name"),
                (String)doc.get("pwd"),
                (String)doc.get("photoId"),
                (String[]) ((List<String>)doc.get("channelIds")).toArray()
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getPhotoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public String[] getChannelIds() {
        return channelIds == null ? new String[0] : channelIds;
    }

    public void setChannelIds(String[] channelIds) {
        this.channelIds = channelIds;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", name=" + name + ", pwd=" + pwd + ", photoId=" + photoId + ", channelIds="
                + Arrays.toString(channelIds) + "]";
    }

    public Document toDocument() {
        return new Document("_id", id)
                .append("name", name)
                .append("pwd", pwd)
                .append("photoId", photoId)
                .append("channelIds", Arrays.asList(channelIds));
    }
}
