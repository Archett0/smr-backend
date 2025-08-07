package com.team12.listingservice.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PROPERTY_SYNC_EXCHANGE = "property.sync.exchange";
    public static final String PROPERTY_SYNC_ROUTING_KEY = "property.sync.key";

    public static final String USER_SYNC_EXCHANGE = "user.sync.exchange";
    public static final String USER_SYNC_ROUTING_KEY = "user.sync.key";

    @Bean
    public TopicExchange propertyExchange() {
        return new TopicExchange(PROPERTY_SYNC_EXCHANGE);
    }

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_SYNC_EXCHANGE);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}