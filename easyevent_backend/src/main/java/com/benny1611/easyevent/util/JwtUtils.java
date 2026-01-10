package com.benny1611.easyevent.util;

import com.benny1611.easyevent.dao.RoleRepository;
import com.benny1611.easyevent.dao.UserRepository;
import com.benny1611.easyevent.entity.Role;
import com.benny1611.easyevent.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtils {

    private static final Logger LOG = LoggerFactory.getLogger(JwtUtils.class);

    private final SecretKey key = Jwts.SIG.HS256.key().build();
    private final long expiration = TimeUnit.HOURS.toMillis(12);

    public String generateToken(String username, User user) {
        List<String> roles = user.getRoles().stream().map(Role::getName).toList();
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .claim("roles", roles)
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (JwtException e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public List<GrantedAuthority> getAuthorities(String token, List<Role> allRoles) {
        Object rolesObj = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("roles");
        List<String> rolesStringList = List.of();
        if (rolesObj != null) {
            try {
                rolesStringList = (List<String>) rolesObj;
            } catch (ClassCastException e) {
                LOG.error("Could not cast authorities to list: {}", rolesObj);
                return null;
            }
        }
        List<GrantedAuthority> roles = new ArrayList<>();
        List<String> allRolesString = allRoles.stream().map(Role::getName).toList();

        for (String role: rolesStringList) {
            if (allRolesString.contains(role)) {
                roles.add(new SimpleGrantedAuthority(role));
            }
        }

        return roles;
    }
}
