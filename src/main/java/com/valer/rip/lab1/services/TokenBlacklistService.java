package com.valer.rip.lab1.services;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class TokenBlacklistService{


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JwtService jwtService;

    public void addToBlacklist(HttpServletRequest request) {
        String token = jwtService.extractTokenFromRequest(request);
        Date expiry = jwtService.extractExpiration(token);
        // Calculate the remaining time to expiration
        long expiration = expiry.getTime() - System.currentTimeMillis();
        redisTemplate.opsForValue().set(token, "blacklisted", expiration, TimeUnit.MILLISECONDS);
    }

    public Boolean isBlacklisted(String token) {
        return redisTemplate.hasKey(token);
    }
}