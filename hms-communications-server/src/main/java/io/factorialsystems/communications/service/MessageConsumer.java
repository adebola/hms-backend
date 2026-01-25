package io.factorialsystems.communications.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.factorialsystems.communications.config.RabbitMQConfig;
import io.factorialsystems.communications.model.dto.request.SendEmailRequest;
import io.factorialsystems.communications.model.dto.request.SendSmsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageConsumer {

    private final EmailService emailService;
    private final SmsService smsService;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRIES = 3;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void handleEmailMessage(Message message, Channel channel,
                                   @Header(value = "x-retry-count", required = false) Integer retryCount)
            throws IOException {

        Integer currentRetryCount = retryCount != null ? retryCount : 0;
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        log.debug("Received email message, retry count: {}", currentRetryCount);

        if (currentRetryCount >= MAX_RETRIES) {
            log.error("Email message exceeded max retries ({}), discarding message", MAX_RETRIES);
            channel.basicAck(deliveryTag, false);
            return;
        }

        try {
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);
            SendEmailRequest request = objectMapper.readValue(payload, SendEmailRequest.class);

            log.info("Processing email message for tenant: {} to: {}",
                    request.getTenantId(), request.getToEmail());

            emailService.sendEmail(request.getTenantId(), request);

            // Acknowledge successful processing
            channel.basicAck(deliveryTag, false);
            log.info("Email message processed successfully");

        } catch (Exception e) {
            log.error("Error processing email message: {}", e.getMessage(), e);

            // Reject and requeue only if below max retries
            if (currentRetryCount < MAX_RETRIES) {
                log.info("Republishing email message with retry count: {}", currentRetryCount + 1);
                republishWithRetry(message, currentRetryCount + 1, RabbitMQConfig.EMAIL_ROUTING_KEY);
                channel.basicAck(deliveryTag, false);  // Acknowledge original message
            } else {
                log.error("Max retries reached, discarding email message");
                channel.basicAck(deliveryTag, false);
            }
        }
    }

    @RabbitListener(queues = RabbitMQConfig.SMS_QUEUE)
    public void handleSmsMessage(Message message, Channel channel,
                                @Header(value = "x-retry-count", required = false) Integer retryCount)
            throws IOException {

        Integer currentRetryCount = retryCount != null ? retryCount : 0;
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        log.debug("Received SMS message, retry count: {}", currentRetryCount);

        if (currentRetryCount >= MAX_RETRIES) {
            log.error("SMS message exceeded max retries ({}), discarding message", MAX_RETRIES);
            channel.basicAck(deliveryTag, false);
            return;
        }

        try {
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);
            SendSmsRequest request = objectMapper.readValue(payload, SendSmsRequest.class);

            log.info("Processing SMS message for tenant: {} to: {}",
                    request.getTenantId(), request.getToPhone());

            smsService.sendSms(request.getTenantId(), request);

            // Acknowledge successful processing
            channel.basicAck(deliveryTag, false);
            log.info("SMS message processed successfully");

        } catch (Exception e) {
            log.error("Error processing SMS message: {}", e.getMessage(), e);

            // Reject and requeue only if below max retries
            if (currentRetryCount < MAX_RETRIES) {
                log.info("Republishing SMS message with retry count: {}", currentRetryCount + 1);
                republishWithRetry(message, currentRetryCount + 1, RabbitMQConfig.SMS_ROUTING_KEY);
                channel.basicAck(deliveryTag, false);  // Acknowledge original message
            } else {
                log.error("Max retries reached, discarding SMS message");
                channel.basicAck(deliveryTag, false);
            }
        }
    }

    private void republishWithRetry(Message message, int retryCount, String routingKey) {
        try {
            org.springframework.amqp.core.MessageProperties props = message.getMessageProperties();
            props.getHeaders().put("x-retry-count", retryCount);

            Message retryMessage = new Message(message.getBody(), props);

            rabbitTemplate.send(RabbitMQConfig.COMMUNICATIONS_EXCHANGE, routingKey, retryMessage);

            log.debug("Message republished with retry count: {}", retryCount);

        } catch (Exception e) {
            log.error("Failed to republish message: {}", e.getMessage(), e);
        }
    }
}
