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


import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.valer.rip.lab1.models.ConnectionRequest;
import com.valer.rip.lab1.models.User;
import com.valer.rip.lab1.repositories.ConnectionRequestRepository;
import com.valer.rip.lab1.repositories.UserRepository;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;


@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ConnectionRequestRepository connectionRequestRepository;

    @Autowired
    JwtService jwtService;

    @Autowired
    TokenBlacklistService tokenBlacklistService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    ModelMapper modelMapper;

    @PostConstruct
    public void setupMapper() {
        modelMapper.getConfiguration().setSkipNullEnabled(true).setMatchingStrategy(MatchingStrategies.STRICT);

        modelMapper.typeMap(UserDTO.class, User.class)
            .addMappings(mapper -> mapper.map(src -> src.getRole() != null ? Role.valueOf(src.getRole()) : Role.BUYER, User::setRole));

        modelMapper.typeMap(User.class, UserDTO.class)
            .addMappings(mapper -> mapper.map(User::getRole, (dest, v) -> {
                if (v == null) {
                    dest.setRole(Role.BUYER.name());
                } else {
                    dest.setRole(((Role) v).name());
                }
            }));
    }

    public UserDTO createUser(UserDTO userRequest) throws Exception {
        if(userRequest.getLogin() == null || userRequest.getPassword() == null){
            throw new Exception("Параметры login и password не могут быть пустыми!");
        }

        User user = modelMapper.map(userRequest, User.class);

        // if (userRequest.getRole() == null) {
        //     user.setRole(Role.valueOf("BUYER"));
        // }
        // else {
        //     user.setRole(Role.valueOf(userRequest.getRole()));
        // }
        user.setPassword(encodePassword(userRequest.getPassword()));

        User savedUser = userRepository.save(user);
        UserDTO userResponse = modelMapper.map(savedUser, UserDTO.class);
        //userResponse.setRole(user.getRole().name());
        return userResponse;
    }

    public UserDTO updateUser(UserDTO userRequest) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetail = (UserDetails) authentication.getPrincipal();
        String username = userDetail.getUsername();
        
        User currentUser = userRepository.findByLogin(username).orElseThrow(() -> new Exception("Пользователь не найден!"));

        if (userRequest.getPassword() != null) {
            userRequest.setPassword(encodePassword(userRequest.getPassword()));
        }

        modelMapper.map(userRequest, currentUser);
        // if (userRequest.getRole() != null) {
        //     currentUser.setRole(Role.valueOf(userRequest.getRole()));
        // }

        UserDTO userResponse = modelMapper.map(currentUser, UserDTO.class);
        //userResponse.setRole(currentUser.getRole().name());
        return userResponse;
    }

    // public UserDTO getUserProfile() throws Exception {
    //     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //     UserDetails userDetail = (UserDetails) authentication.getPrincipal();
    //     String username = userDetail.getUsername();
    //     User user = userRepository.findByLogin(username)
    //         .orElseThrow(() -> new Exception("Пользователь не найден!"));

    //     UserDTO userResponse = modelMapper.map(user, UserDTO.class);
    //     userResponse.setRole(user.getRole().name());
    //     return userResponse;
    // }

    // public UserDTO getUserById(int id) throws Exception {
        
    //     if (id == 0) {
    //         throw new IllegalArgumentException("ID не может быть нулевым");
    //     }

    //     User user = userRepository.findById(id).orElseThrow(() -> new Exception("Не удалось найти пользователя по ID: " + id));

    //     UserDTO userResponse  = modelMapper.map(user, UserDTO.class);
    //     userResponse.setRole(user.getRole().name());
    //     return userResponse;
    // }

    public JwtResponseDTO loginUser(AuthRequestDTO authRequestDTO) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequestDTO.getLogin(), authRequestDTO.getPassword()));
        if(authentication.isAuthenticated()){
            return jwtService.GenerateToken(authRequestDTO.getLogin());
        } 
        else {
            throw new UsernameNotFoundException("Пользователя не удалось залогинить!");
        }
    }

    public String logoutUser(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetail = (UserDetails) authentication.getPrincipal();
        String username = userDetail.getUsername();
        int userId = userRepository.findByLogin(username).get().getId();
        tokenBlacklistService.addToBlacklist(request, userId);
        return "Вы успешно разлогинились!";
    }

    public boolean isOwnerOfRequest(int requestId, String username) {
        ConnectionRequest request = connectionRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Заявка " + requestId + " пользователя " + username + " не найдена!"));
        return request.getClient().getLogin().equals(username);
    }

    public String encodePassword(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }
}