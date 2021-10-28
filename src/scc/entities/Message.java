package scc.entities;

public class Message {
    private int id;
    private int senderId;
    private int mediaId;
    private int channelId;

    private int replyId;

    public Message() {
    }

    public Message(int id, int senderId, int mediaId, int channelId, int replyId) {
        this.id = id;
        this.senderId = senderId;
        this.mediaId = mediaId;
        this.channelId = channelId;
        this.replyId = replyId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getName() {
        return senderId;
    }

    public void setName(int name) {
        this.senderId = name;
    }

    public int getMediaId() {
        return mediaId;
    }

    public void setMediaId(int mediaId) {
        this.mediaId = mediaId;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public int getReplyId() {
        return replyId;
    }

    public void setReplyId(int replyId) {
        this.replyId = replyId;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", name=" + senderId + ", pwd=" + mediaId + ", photoId=" + channelId + "]";
    }
}
