package io.factorialsystems.communications.service;

import io.factorialsystems.communications.config.CommunicationsProperties;
import io.factorialsystems.communications.exception.MessageSendException;
import io.factorialsystems.communications.model.entity.EmailMessage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sendinblue.ApiClient;
import sendinblue.ApiException;
import sendinblue.Configuration;
import sibApi.TransactionalEmailsApi;
import sibModel.CreateSmtpEmail;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailAttachment;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrevoEmailProvider {

    private final CommunicationsProperties properties;
    private TransactionalEmailsApi apiInstance;

    @PostConstruct
    public void init() {
        log.info("Initializing Brevo email provider");
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setApiKey(properties.getBrevo().getApiKey());
        this.apiInstance = new TransactionalEmailsApi();
        log.info("Brevo email provider initialized successfully");
    }

    public String sendEmail(EmailMessage message) throws MessageSendException {
        try {
            SendSmtpEmail email = new SendSmtpEmail();

            // Set sender
            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail(message.getFromEmail());
            sender.setName(message.getFromName());
            email.setSender(sender);

            // Set recipient
            List<SendSmtpEmailTo> toList = new ArrayList<>();
            SendSmtpEmailTo to = new SendSmtpEmailTo();
            to.setEmail(message.getToEmail());
            to.setName(message.getToName());
            toList.add(to);
            email.setTo(toList);

            // Set subject and content
            email.setSubject(message.getSubject());
            if (message.getHtmlContent() != null) {
                email.setHtmlContent(message.getHtmlContent());
            }
            if (message.getTextContent() != null) {
                email.setTextContent(message.getTextContent());
            }

            // Set attachments if any
            if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
                List<SendSmtpEmailAttachment> attachments = new ArrayList<>();
                for (Map<String, String> attachmentData : message.getAttachments()) {
                    SendSmtpEmailAttachment attachment = new SendSmtpEmailAttachment();
                    attachment.setName(attachmentData.get("filename"));
                    attachment.setContent(attachmentData.get("content").getBytes());  // Base64 encoded string to bytes
                    attachments.add(attachment);
                }
                email.setAttachment(attachments);
            }

            // Send email
            CreateSmtpEmail response = apiInstance.sendTransacEmail(email);

            log.info("Email sent successfully via Brevo, messageId: {}", response.getMessageId());
            return response.getMessageId();

        } catch (ApiException e) {
            log.error("Failed to send email via Brevo: {}", e.getMessage(), e);
            throw new MessageSendException("Failed to send email via Brevo: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending email: {}", e.getMessage(), e);
            throw new MessageSendException("Unexpected error sending email: " + e.getMessage(), e);
        }
    }
}
