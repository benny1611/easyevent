package com.benny1611.easyevent.dao;

import com.benny1611.easyevent.entity.OauthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OauthProviderRepository extends JpaRepository<OauthProvider, Short> {
    Optional<OauthProvider> findByNameIgnoreCase(String name);
}
