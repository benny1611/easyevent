package com.benny1611.easyevent.service;

import com.benny1611.easyevent.entity.User;

public interface IMailService {
    void sendPasswordResetEmail(User user, String resetLink, int expiryMinutes);
}
