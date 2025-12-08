package br.com.ifba.sididoc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${MAIL_SENDER_EMAIL}")
    private String senderEmail;

    @Value("${MAIL_SENDER_NAME}")
    private String senderName;

    //Enviar email para um destinatário
    public void send(String to, String subject, String text) {
        SimpleMailMessage msg = new SimpleMailMessage();

        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);
        msg.setFrom(String.format("%s <%s>", senderName, senderEmail));

        mailSender.send(msg);
    }
}

