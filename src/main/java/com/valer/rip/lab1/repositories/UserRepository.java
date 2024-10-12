package com.valer.rip.lab1.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.valer.rip.lab1.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByLoginAndPassword(String login, String password);

    User findByLogin(String login);
}