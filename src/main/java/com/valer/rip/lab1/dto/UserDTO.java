package com.valer.rip.lab1.dto;

import com.valer.rip.lab1.helpers.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private int id;
    private String login;
    private String password;
    private String role; 
}
