package scc.entities;

public class Session {
    String uid;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    String username;
    public Session(String uid,String username){
        this.uid = uid;
        this.username = username;
    }
}
