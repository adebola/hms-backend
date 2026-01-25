package io.factorialsystems.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String AUTH_EVENTS_EXCHANGE = "hms.auth.events";
    public static final String COMMUNICATIONS_EXCHANGE = "hms.communications.events";
    public static final String EMAIL_ROUTING_KEY = "email.send";
    public static final String SMS_ROUTING_KEY = "sms.send";

    @Bean
    public TopicExchange authEventsExchange() {
        return ExchangeBuilder.topicExchange(AUTH_EVENTS_EXCHANGE).durable(true).build();
    }

    @Bean
    public TopicExchange communicationsExchange() {
        return ExchangeBuilder.topicExchange(COMMUNICATIONS_EXCHANGE).durable(true).build();
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(om);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
