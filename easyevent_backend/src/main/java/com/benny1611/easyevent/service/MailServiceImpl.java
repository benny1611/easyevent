package com.benny1611.easyevent.service;

import com.benny1611.easyevent.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class MailServiceImpl implements IMailService {

    private final JavaMailSender mailSender;
    private final MessageSource mailMessageSource;

    @Value("${app.mail.from}")
    private String from;

    @Autowired
    public MailServiceImpl(JavaMailSender mailSender, MessageSource mailMessageSource) {
        this.mailSender = mailSender;
        this.mailMessageSource = mailMessageSource;
    }

    @Override
    public void sendPasswordResetEmail(User user, String resetLink, int expiryMinutes) {
        Locale locale = resolveLocale(user);

        String subject = mailMessageSource.getMessage(
                "password.reset.subject",
                null,
                locale
        );

        String body = mailMessageSource.getMessage(
                "password.reset.body",
                new Object[]{user.getName(), resetLink, expiryMinutes},
                locale
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    private Locale resolveLocale(User user) {
        return user.getLanguage() != null
                ? Locale.forLanguageTag(user.getLanguage())
                : Locale.ENGLISH;
    }
}
