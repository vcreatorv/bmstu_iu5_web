package com.valer.rip.lab1.controllers;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.ModelAttribute;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.valer.rip.lab1.dto.UserDTO;
// import com.valer.rip.lab1.services.UserService;


// @RestController
// @RequestMapping("/api/users")
// public class UserController {

//     private final UserService userService;

//     public UserController(UserService userService) {
//         this.userService = userService;
//     }

//     @PostMapping("/create")
//     public ResponseEntity<? extends Object> createUser(@ModelAttribute UserDTO userDTO) {
//         try {
//             return ResponseEntity.status(HttpStatus.OK).body(userService.createUser(userDTO));
//         }
//         catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//         }
//     }

//     @PostMapping("/authenticate")
//     public ResponseEntity<? extends Object> authenticateUser(@ModelAttribute UserDTO userDTO) {
//         try {
//             return ResponseEntity.status(HttpStatus.OK).body(userService.authenticateUser(userDTO));
//         }
//         catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//         }
//     }
    
//     @PostMapping("/logout")
//     public ResponseEntity<? extends Object> logoutUser(@ModelAttribute UserDTO userDTO) {
//         try {
//             return ResponseEntity.status(HttpStatus.OK).body(userService.logoutUser(userDTO));
//         }
//         catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//         }
//     }

//     @PutMapping("/{userID}/update")
//     public ResponseEntity<? extends Object> updateUser(@PathVariable("userID") int userID, @ModelAttribute UserDTO userDTO) {
//         try {
//             return ResponseEntity.status(HttpStatus.OK).body(userService.updateUser(userID, userDTO));
//         }
//         catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//         }
//     }
// }

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.valer.rip.lab1.dto.AuthRequestDTO;
import com.valer.rip.lab1.dto.JwtResponseDTO;
import com.valer.rip.lab1.services.UserService;

import java.util.List;

import com.valer.rip.lab1.dto.UserDTO;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping(value = "/save")
    public ResponseEntity saveUser(@ModelAttribute UserDTO userRequest) {
        try {
            UserDTO userResponse = userService.saveUser(userRequest);
            return ResponseEntity.ok(userResponse);
        } 
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/profile")
    public ResponseEntity<UserDTO> getUserProfile() {
        try {
            UserDTO userResponse = userService.getLoggedInUserProfile();
            return ResponseEntity.ok().body(userResponse);
        } 
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    // @GetMapping("/user/{id}")
    // public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
    //     try {
    //         UserDTO userResponse = userService.getUserById(id);
    //         return ResponseEntity.ok().body(userResponse);
    //     } 
    //     catch (Exception e){
    //         throw new RuntimeException(e);
    //     }
    // }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> AuthenticateAndGetToken(@ModelAttribute AuthRequestDTO authRequestDTO){
        try {
            return ResponseEntity.ok().body(userService.login(authRequestDTO));
        } 
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        return ResponseEntity.ok(userService.logout(request));
    }

}