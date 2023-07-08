package com.smart.entities;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class EmailRequest {
    private String to;
    private String subject;
    private String message;
    private MultipartFile attachment;

    public EmailRequest(String to, String subject, String message, MultipartFile attachment) {
        this.to = to;
        this.subject = subject;
        this.message = message;
        this.attachment = attachment;
    }

    public EmailRequest() {
        super();
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MultipartFile getAttachment() {
        return attachment;
    }

    public void setAttachment(MultipartFile attachment) {
        this.attachment = attachment;
    }

    @Override
    public String toString() {
        return "EmailRequest [to=" + to + ", subject=" + subject + ", message=" + message + ", attachment=" + attachment
                + "]";
    }
}
