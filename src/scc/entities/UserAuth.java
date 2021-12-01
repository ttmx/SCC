package scc.entities;

public class UserAuth {
    String user;
    String pwd;

    public UserAuth() {

    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public boolean isValid() {
        return user != null && pwd != null;
    }

}
