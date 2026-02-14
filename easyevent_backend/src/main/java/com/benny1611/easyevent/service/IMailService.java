package com.benny1611.easyevent.service;

import com.benny1611.easyevent.entity.User;

import java.util.UUID;

public interface IMailService {
    void sendPasswordResetEmail(User user, UUID tokenId, String secret, int expiryMinutes);
    void sendActivationEmail(User user);
}
