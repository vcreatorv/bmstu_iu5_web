package com.valer.rip.lab1.services;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class TokenBlacklistService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtService jwtService;

    public void addToBlacklist(HttpServletRequest request, int userId) {
        String token = jwtService.extractTokenFromRequest(request);
        Date expiry = jwtService.extractExpiration(token);
        long expiration = expiry.getTime() - System.currentTimeMillis();
        redisTemplate.opsForValue().set(((Integer) userId).toString(), "jwt." + token, expiration, TimeUnit.MILLISECONDS);
    }

    public Boolean isBlacklisted(String token, int userId) {
        String blacklistedToken = redisTemplate.opsForValue().get(((Integer) userId).toString());
        return blacklistedToken != null && blacklistedToken.equals("jwt." + token);
    }
}