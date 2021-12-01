package scc.entities;

import org.bson.Document;

import java.util.Arrays;
import java.util.List;

public class User {

    public static final String ID = "_id";
    public static final String NAME = "name";
    public static final String PWD = "pwd";
    public static final String PHOTOID = "photoId";
    public static final String CHANNELIDS = "channelIds";


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
                (String) doc.get(ID),
                (String) doc.get(NAME),
                (String) doc.get(PWD),
                (String) doc.get(PHOTOID),
                ((List<String>) doc.get(CHANNELIDS)).toArray(new String[0])
        );
    }

    public String getId() {
        return id;
    }

    public User setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public String getPwd() {
        return pwd;
    }

    public User setPwd(String pwd) {
        this.pwd = pwd;
        return this;
    }

    public String getPhotoId() {
        return photoId;
    }

    public User setPhotoId(String photoId) {
        this.photoId = photoId;
        return this;
    }

    public String[] getChannelIds() {
        return channelIds == null ? new String[0] : channelIds;
    }

    public User setChannelIds(String[] channelIds) {
        this.channelIds = channelIds;
        return this;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", name=" + name + ", pwd=" + pwd + ", photoId=" + photoId + ", channelIds="
                + Arrays.toString(channelIds) + "]";
    }

    public Document toDocument() {
        return new Document(ID, id)
                .append(NAME, name)
                .append(PWD, pwd)
                .append(PHOTOID, photoId)
                .append(CHANNELIDS, Arrays.asList(channelIds));
    }
}
