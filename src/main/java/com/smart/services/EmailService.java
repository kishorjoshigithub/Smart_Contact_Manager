package com.smart.services;
import java.util.Properties;

import javax.activation.DataHandler;
import org.springframework.stereotype.Service;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;

import org.springframework.web.multipart.MultipartFile;

@Service
public class EmailService {

    public boolean sendEmail(String subject, String message, String to, MultipartFile attachment) {
        String from = "kishorprasadjoshi111@gmail.com";
        boolean success = false;

        // Variable for Gmail
        String host = "smtp.gmail.com";

        // Get the system properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.auth", "true");

        // Create a Session object
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication("kishorprasadjoshi111@gmail.com", "pewdpckgjpnpjuav");
            }
        });

        session.setDebug(true);

        try {
            // Create a MimeMessage object
            MimeMessage mimeMessage = new MimeMessage(session);

            // Set From Address
            mimeMessage.setFrom(new InternetAddress(from));

            // Set To Address
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Email Subject
            mimeMessage.setSubject(subject);

            // Create the message body part for the text message
            MimeBodyPart textBodyPart = new MimeBodyPart();
            textBodyPart.setText(message);

            // Create a multipart message to hold both text and attachment
            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(textBodyPart);

            // Add attachment if available
            if (attachment != null && !attachment.isEmpty()) {
                MimeBodyPart attachmentBodyPart = new MimeBodyPart();
                DataSource source = new ByteArrayDataSource(attachment.getBytes(), attachment.getContentType());
                attachmentBodyPart.setDataHandler(new DataHandler(source));
                attachmentBodyPart.setFileName(attachment.getOriginalFilename());
                multipart.addBodyPart(attachmentBodyPart);
            }

            // Set the content of the email
            mimeMessage.setContent(multipart);

            // Send Email
            Transport.send(mimeMessage);

            System.out.println("Email Sent Successfully.");
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return success;
    }
}
