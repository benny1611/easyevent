package com.benny1611.easyevent.service;

import com.benny1611.easyevent.dao.OauthProviderRepository;
import com.benny1611.easyevent.dao.UserOAuthAccountRepository;
import com.benny1611.easyevent.entity.OauthProvider;
import com.benny1611.easyevent.entity.User;
import com.benny1611.easyevent.entity.UserOAuthAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class UserOAuthAccountService {

    private final UserOAuthAccountRepository userOAuthAccountRepository;
    private final OauthProviderRepository oauthProviderRepository;

    @Autowired
    public UserOAuthAccountService(UserOAuthAccountRepository userOAuthAccountRepository, OauthProviderRepository oauthProviderRepository) {
        this.userOAuthAccountRepository = userOAuthAccountRepository;
        this.oauthProviderRepository = oauthProviderRepository;
    }


    public Optional<UserOAuthAccount> findByProviderAndProviderUserId(String providerRegistrationId, String providerUserId) {
        return userOAuthAccountRepository.findByProvider_NameIgnoreCaseAndProviderUserId(providerRegistrationId.toUpperCase(), providerUserId);
    }

    @Transactional
    public UserOAuthAccount linkAccount(User user, String providerRegistrationId, String providerUserId, String email) {
        OauthProvider provider = oauthProviderRepository.findByNameIgnoreCase(providerRegistrationId.toUpperCase()).orElseThrow(() -> new IllegalStateException("Unknown OAuth provider"));
        UserOAuthAccount account = new UserOAuthAccount();
        account.setUser(user);
        account.setProvider(provider);
        account.setProviderUserId(providerUserId);
        account.setEmail(email);
        account.setConnectedAt(OffsetDateTime.now());

        return userOAuthAccountRepository.save(account);
    }
}
