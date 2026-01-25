package io.factorialsystems.communications.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    public static final String COMMUNICATIONS_EXCHANGE = "hms.communications.events";
    public static final String EMAIL_QUEUE = "communications.email.send";
    public static final String SMS_QUEUE = "communications.sms.send";
    public static final String EMAIL_ROUTING_KEY = "email.send";
    public static final String SMS_ROUTING_KEY = "sms.send";

    @Bean
    public TopicExchange communicationsExchange() {
        return ExchangeBuilder
                .topicExchange(COMMUNICATIONS_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder
                .durable(EMAIL_QUEUE)
                .build();
    }

    @Bean
    public Queue smsQueue() {
        return QueueBuilder
                .durable(SMS_QUEUE)
                .build();
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, TopicExchange communicationsExchange) {
        return BindingBuilder
                .bind(emailQueue)
                .to(communicationsExchange)
                .with(EMAIL_ROUTING_KEY);
    }

    @Bean
    public Binding smsBinding(Queue smsQueue, TopicExchange communicationsExchange) {
        return BindingBuilder
                .bind(smsQueue)
                .to(communicationsExchange)
                .with(SMS_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}
