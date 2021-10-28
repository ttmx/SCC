package scc.entities;

import java.util.List;

public class Channel {
    public enum Status {
        PRIVATE,
        PUBLIC
    }

    private String name;
    private int id;
    private int ownerId;
    private List<Integer> userIdList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public List<Integer> getUserIdList() {
        return userIdList;
    }

    public void setUserIdList(List<Integer> userIdList) {
        this.userIdList = userIdList;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    private Status status;

    public Channel() {
    }


    public Channel(String name, int id, Status status, int ownerId, List<Integer> userIdList) {
        this.name = name;
        this.id = id;
        this.status = status;
        this.ownerId = ownerId;
        this.userIdList = userIdList;
    }

}
