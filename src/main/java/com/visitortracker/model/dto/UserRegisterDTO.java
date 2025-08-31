package com.visitortracker.model.dto;


import com.visitortracker.model.Role;

import java.util.Set;

public class UserRegisterDTO {
    private String username;
    private String password;


    private Role roles; //  ["ADMIN", "RECEPTIONIST"]

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

    public Role getRoles() {
        return roles;
    }

    public void setRoles(Role roles) {
        this.roles = roles;
    }

}
