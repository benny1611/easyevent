package com.benny1611.easyevent.dao;

import com.benny1611.easyevent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("""
    SELECT DISTINCT u
    FROM User u
    LEFT JOIN FETCH u.roles
    WHERE u.email = :email
    """)
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    @Query("""
    SELECT DISTINCT u
    FROM User u
    LEFT JOIN FETCH u.roles
    WHERE u.id = :id
    """)
    Optional<User> findByIdWithRoles(@Param("id") Long id);

    @Query("""
        SELECT DISTINCT u
        FROM User u
        LEFT JOIN FETCH u.roles r
        JOIN FETCH u.state s
        WHERE u.email = :email
    """)
    Optional<User> findByEmailWithRolesAndState(@Param("email") String email);

    Optional<User> findByEmail(@Param("email") String email);
}
