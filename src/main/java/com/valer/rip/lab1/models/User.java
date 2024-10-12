package com.valer.rip.lab1.models;

import com.valer.rip.lab1.helpers.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 255, nullable = false)
    private String login;

    @Column(length = 150, nullable = false)
    private String password;

    @Column(nullable = false)
    private int role;

    public User() {
        this.role = Role.BUYER.getValue();
    }

    public Role getRole() {
        return Role.fromValue(role);
    }

    public void setRole(Role role) {
        this.role = role.getValue();
    }
}
