package scc.entities;

import org.bson.Document;

import java.util.Arrays;
import java.util.List;

public class Message {
    private String id;
    private String replyTo;
    private String channel;
    private String user;
    private String text;
    private String imageId;

    public Message() {
    }

    public Message(String id, String replyTo, String channel, String user, String text, String imageId) {
        this.id = id;
        this.replyTo = replyTo;
        this.channel = channel;
        this.user = user;
        this.text = text;
        this.imageId = imageId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReplyTo() {
        return this.replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public String getChannel() {
        return this.channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageId() {
        return this.imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    @Override
    public String toString() {
        return "Message [id=" + this.id + ", replyTo=" + this.replyTo + ", channel=" + this.channel + ", user=" + this.user + ", text=" + this.text + ", imageId=" + imageId + "]";
    }

    static public Message fromDocument(Document doc) {
        return new Message (
                (String)doc.get("_id"),
                (String)doc.get("replyTo"),
                (String)doc.get("channel"),
                (String)doc.get("user"),
                (String)doc.get("text"),
                (String)doc.get("imageId")
        );
    }

    public Document toDocument() {
        return new Document("_id", id)
                .append("replyTo", replyTo)
                .append("channel", channel)
                .append("user", user)
                .append("text", text)
                .append("imageId", imageId);
    }
}
