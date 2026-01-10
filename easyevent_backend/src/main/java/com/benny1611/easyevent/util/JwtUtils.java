package com.benny1611.easyevent.util;

import com.benny1611.easyevent.dao.UserRepository;
import com.benny1611.easyevent.entity.Role;
import com.benny1611.easyevent.entity.User;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtils {

    private final SecretKey key = Jwts.SIG.HS256.key().build();
    private final long expiration = TimeUnit.HOURS.toMillis(12);

    public String generateToken(String username, UserRepository userRepository) {
        Optional<User> userOptional = userRepository.findByEmailWithRoles(username);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
        } else {
            throw new UsernameNotFoundException("Username not found");
        }
        List<String> roles = user.getRoles().stream().map(Role::getName).toList();
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .claim("roles", roles)
                .signWith(key)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
