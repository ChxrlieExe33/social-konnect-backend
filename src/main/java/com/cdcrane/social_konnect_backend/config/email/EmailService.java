package com.cdcrane.social_konnect_backend.config.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
@Slf4j
public class EmailService implements EmailUseCase {

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${spring.mail.password}")
    private String senderPassword;

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private int mailPort;

    @Override
    public void sendVerificationEmail(String email, int verificationCode) {

        Properties props = prepareProperties();

        Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, senderPassword);
                }
            }
        );

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Social Konnect Verification Code");
            message.setText("Your verification code is:\n\n" + verificationCode);

            Transport.send(message);

            System.out.println("Email sent to " + email);

        } catch (MessagingException e) {

            throw new RuntimeException("Email failed to send to " + email + " ." + e);

        }

    }

    private Properties prepareProperties() {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", mailHost);
        props.put("mail.smtp.port", String.valueOf(mailPort));

        return props;
    }

}
