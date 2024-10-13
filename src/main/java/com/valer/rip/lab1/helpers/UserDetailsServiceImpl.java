package com.valer.rip.lab1.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.valer.rip.lab1.models.User;
import com.valer.rip.lab1.repositories.UserRepository;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {

        logger.debug("Запуск метода loadUserByUsername...");
        User user = userRepository.findByLogin(login).orElseThrow(() -> new UsernameNotFoundException("Не удалось найти пользователя..."));
        logger.info("Пользователь прошел аутентификацию!");
        return new CustomUserDetails(user);
    }
}