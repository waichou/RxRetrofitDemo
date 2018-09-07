package com.jimi_wu.sample.model;

/**
 * Created by Administrator on 2016/11/9.
 */
public class UserBean extends ResultBean<UserBean>{

    public UserBean(String username, String password) {
        this.username = username;
        this.password = password;
    }

    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User [name=" + username + ", password=" + password + "]";
    }

}
