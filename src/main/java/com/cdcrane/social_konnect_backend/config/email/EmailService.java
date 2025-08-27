package com.cdcrane.social_konnect_backend.config.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.UUID;

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

    private Properties prepareProperties() {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", mailHost);
        props.put("mail.smtp.port", String.valueOf(mailPort));

        return props;
    }

    @Override
    public void sendSignupVerificationEmail(String email, String username, int verificationCode) {

        Properties props = prepareProperties();

        Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(senderEmail, senderPassword);
                    }
                }
        );

        String verificationUrl = "http://localhost:4200/auth/register?username=" + username;

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Social Konnect Verification Code");

            String html = """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
            </head>
            <body style="margin:0; padding:0; font-family: Arial, Helvetica, sans-serif; background-color: #ffffff;">
              <table align="center" width="100%%" cellpadding="0" cellspacing="0" border="0">
                <tr>
                  <td align="center" style="padding: 40px 20px;">
                    <table width="600" cellpadding="0" cellspacing="0" border="0" style="max-width:600px; width:100%%;">
                      <!-- Title -->
                      <tr>
                        <td align="center" style="font-size: 28px; font-weight: bold; color: #56A5FF; padding-bottom: 20px;">
                          Social Konnect
                        </td>
                      </tr>
                      <!-- Greeting -->
                      <tr>
                        <td align="center" style="font-size: 18px; padding-bottom: 15px;">
                          Welcome to Social Konnect %s,
                        </td>
                      </tr>
                      <!-- Code -->
                      <tr>
                        <td align="center" style="font-size: 18px; padding: 10px 20px; background-color: #f4f4f4; border-radius: 6px;">
                          Your verification code is: <b style="font-size: 22px; color: #333;">%d</b>
                        </td>
                      </tr>
                      <!-- Button with URL -->
                      <tr>
                        <td align="center" style="padding-top: 30px; padding-bottom: 20px;">
                          <a href="%s"
                             style="display:inline-block; padding: 12px 24px; font-size: 16px;
                                    color: #ffffff; background-color: #56A5FF; text-decoration: none;
                                    border-radius: 6px; font-weight: bold;">
                            Verify My Account
                          </a>
                        </td>
                      </tr>
                      <!-- Footer -->
                      <tr>
                        <td align="center" style="font-size: 14px; color: #888; padding-top: 20px;">
                          This code will expire in 10 minutes.
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(username, verificationCode, verificationUrl);


            message.setContent(html, "text/html; charset=utf-8");

            Transport.send(message);

            log.info("Email sent to {}", email);

        } catch (MessagingException e) {

            throw new RuntimeException("Email failed to send to " + email + " ." + e);

        }

    }

    @Override
    public void sendForgotPasswordVerificationEmail(String email, String username, int verificationCode, UUID resetSessionId) {

        Properties props = prepareProperties();

        Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(senderEmail, senderPassword);
                    }
                }
        );

        String verificationUrl = "http://localhost:4200/auth/forgot-password?reset-id=" + resetSessionId.toString();

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Social Konnect Verification Code");

            String html = """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
            </head>
            <body style="margin:0; padding:0; font-family: Arial, Helvetica, sans-serif; background-color: #ffffff;">
              <table align="center" width="100%%" cellpadding="0" cellspacing="0" border="0">
                <tr>
                  <td align="center" style="padding: 40px 20px;">
                    <table width="600" cellpadding="0" cellspacing="0" border="0" style="max-width:600px; width:100%%;">
                      <!-- Title -->
                      <tr>
                        <td align="center" style="font-size: 28px; font-weight: bold; color: #56A5FF; padding-bottom: 20px;">
                          Social Konnect
                        </td>
                      </tr>
                      <!-- Greeting -->
                      <tr>
                        <td align="center" style="font-size: 18px; padding-bottom: 15px;">
                          Welcome back to Social Konnect %s,
                        </td>
                      </tr>
                      <!-- Code -->
                      <tr>
                        <td align="center" style="font-size: 18px; padding: 10px 20px; background-color: #f4f4f4; border-radius: 6px;">
                          Your verification code is: <b style="font-size: 22px; color: #333;">%d</b>
                        </td>
                      </tr>
                      <!-- Button with URL -->
                      <tr>
                        <td align="center" style="padding-top: 30px; padding-bottom: 20px;">
                          <a href="%s"
                             style="display:inline-block; padding: 12px 24px; font-size: 16px;
                                    color: #ffffff; background-color: #56A5FF; text-decoration: none;
                                    border-radius: 6px; font-weight: bold;">
                            Give verification code
                          </a>
                        </td>
                      </tr>
                      <!-- Footer -->
                      <tr>
                        <td align="center" style="font-size: 14px; color: #888; padding-top: 20px;">
                          This code will expire in 10 minutes.
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(username, verificationCode, verificationUrl);


            message.setContent(html, "text/html; charset=utf-8");

            Transport.send(message);

            log.info("Email sent to {}", email);

        } catch (MessagingException e) {

            throw new RuntimeException("Email failed to send to " + email + " ." + e);

        }

    }

}
