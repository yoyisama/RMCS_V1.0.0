package com.rmcs.model;

/** 登录用户信息（与原型 AuthUser 对应）。 */
public class AuthUser {

    private String username = "";
    private String name = "未登录";
    private String role = "访客";
    private boolean loggedIn = false;

    public AuthUser() {}

    public AuthUser(String username, String name, String role, boolean loggedIn) {
        this.username = username;
        this.name = name;
        this.role = role;
        this.loggedIn = loggedIn;
    }

    public static AuthUser guest() {
        return new AuthUser("", "未登录", "访客", false);
    }

    public static AuthUser admin() {
        return new AuthUser("admin", "管理员", "超级管理员", true);
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isLoggedIn() { return loggedIn; }
    public void setLoggedIn(boolean loggedIn) { this.loggedIn = loggedIn; }
}