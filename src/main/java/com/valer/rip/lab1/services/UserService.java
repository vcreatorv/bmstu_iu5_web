package com.valer.rip.lab1.services;

// import java.util.Optional;

// import org.modelmapper.ModelMapper;
// import org.springframework.stereotype.Service;

// import com.valer.rip.lab1.dto.UserDTO;
// import com.valer.rip.lab1.models.User;
// import com.valer.rip.lab1.repositories.UserRepository;

// @Service
// public class UserService {
//     private final UserRepository userRepository;
//     private final ModelMapper modelMapper;

//     public UserService(UserRepository userRepository, ModelMapper modelMapper) {
//         this.userRepository = userRepository;
//         this.modelMapper = modelMapper;
//     }

//     public User createUser(UserDTO userDTO) throws Exception {
//         verifyUserCredentials(userDTO);
//         try {
//             User user = new User();
//             modelMapper.map(userDTO, user);
//             return userRepository.save(user);
//         }
//         catch (Exception e) {
//             throw new Exception("Ошибка при создании нового пользователя");
//         }
//     }

//     public User updateUser(User user) {
//         return userRepository.save(user);
//     }

//     public Optional<User> findById(int userID) {
//         return userRepository.findById(userID);
//     }

//     public String authenticateUser(UserDTO userDTO) throws Exception {
//         verifyUserCredentials(userDTO);
//         try {
//             userRepository.findByLoginAndPassword(userDTO.getLogin(), userDTO.getPassword())
//             .orElseThrow(() -> new Exception("Неверно введен логин или пароль"));
            
//             return "Пользователь аутентифицирован";
//         }
//         catch (Exception e) {
//             throw new Exception("Неверно введен логин или пароль");
//         }
//     }

//     public String logoutUser(UserDTO userDTO) throws Exception {
//         verifyUserCredentials(userDTO);
//         try {
//             userRepository.findByLoginAndPassword(userDTO.getLogin(), userDTO.getPassword())
//             .orElseThrow(() -> new Exception("Не удалось деавторизовать пользователя"));
            
//             return "Пользователь деавторизован";
//         }
//         catch (Exception e) {
//             throw new Exception("Не удалось деавторизовать пользователя");
//         }
//     }

//     public User updateUser(int userID, UserDTO userDTO) throws Exception {
//         User user = userRepository.findById(userID)
//             .orElseThrow(() -> new Exception("Такого пользователя не существует"));

//         if (userDTO.getLogin() != null) {
//             user.setLogin(userDTO.getLogin());
//         }

//         if (userDTO.getPassword() != null) {
//             user.setPassword(userDTO.getPassword());
//         }
        
//         return user;
//     }

//     public void verifyUserCredentials(UserDTO userDTO) throws Exception {
//         if (userDTO.getLogin() == null || userDTO.getPassword() == null) {
//             throw new Exception("Поля логин и пароль должны быть заполнены");
//         }
//     }

//     public int getUserID() {
//         return 1;
//     }
// }


import jakarta.servlet.http.HttpServletRequest;

import org.checkerframework.checker.units.qual.A;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.valer.rip.lab1.dto.AuthRequestDTO;
import com.valer.rip.lab1.dto.JwtResponseDTO;
import com.valer.rip.lab1.dto.UserDTO;
import com.valer.rip.lab1.helpers.Role;
import com.valer.rip.lab1.models.User;
import com.valer.rip.lab1.repositories.UserRepository;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtService jwtService;

    @Autowired
    TokenBlacklistService tokenBlacklistService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    ModelMapper modelMapper;



    public UserDTO saveUser(UserDTO userRequest) throws Exception {
        if(userRequest.getLogin() == null){
            throw new RuntimeException("Не удалось найти параметр login в запросе!");
        } 
        else if(userRequest.getPassword() == null){
            throw new RuntimeException("Не удалось найти параметр pasword в запросе!");
        }

        User savedUser = null;
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = userRequest.getPassword();
        String encodedPassword = encoder.encode(rawPassword);

        User user = modelMapper.map(userRequest, User.class);
        
        if (userRequest.getRole() == null) {
            user.setRole(Role.valueOf("BUYER"));
        }
        else {
            user.setRole(Role.valueOf(userRequest.getRole()));
        }
        user.setPassword(encodedPassword);
        if(userRequest.getId() != 0) {
            User oldUser = userRepository.findById(userRequest.getId())
            .orElseThrow(() -> new Exception("Can't find record with identifier: " + userRequest.getId()));
            
            oldUser.setId(user.getId());
            oldUser.setPassword(user.getPassword());
            oldUser.setLogin(user.getLogin());
            oldUser.setRole(user.getRole());

            savedUser = userRepository.save(oldUser);
        } 
        else {
            savedUser = userRepository.save(user);
        }

        UserDTO userResponse = modelMapper.map(savedUser, UserDTO.class);
        userResponse.setRole(user.getRole().name());
        return userResponse;
    }

    public UserDTO getLoggedInUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetail = (UserDetails) authentication.getPrincipal();
        String usernameFromAccessToken = userDetail.getUsername();
        User user = userRepository.findByLogin(usernameFromAccessToken);
        UserDTO userResponse = modelMapper.map(user, UserDTO.class);
        userResponse.setRole(user.getRole().name());
        return userResponse;
    }

    public UserDTO getUserById(int id) throws Exception {
        
        if (id == 0) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        User user = userRepository.findById(id)
        .orElseThrow(() -> new Exception("Can't find record with identifier: " + id));


        UserDTO userResponse  = modelMapper.map(user, UserDTO.class);
        userResponse.setRole(user.getRole().name());
        return userResponse;
    }

    public JwtResponseDTO login(AuthRequestDTO authRequestDTO) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequestDTO.getLogin(), authRequestDTO.getPassword()));
        if(authentication.isAuthenticated()){
            return JwtResponseDTO
                    .builder()
                    .accessToken(jwtService.GenerateToken(authRequestDTO.getLogin()))
                    .build();
        } else {
            throw new UsernameNotFoundException("invalid user request!");
        }
    }

    public String logout(HttpServletRequest request) {
        tokenBlacklistService.addToBlacklist(request);
        return "Logged out successfully";
    }

}