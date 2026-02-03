package com.benny1611.easyevent.dao;

import com.benny1611.easyevent.entity.UserState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserStateRepository extends JpaRepository<UserState, Short> {

    Optional<UserState> findByName(@Param("name")String name);
}
