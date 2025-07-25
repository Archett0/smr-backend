package com.team12.searchservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Property data sync related
    public static final String PROPERTY_SYNC_EXCHANGE = "property.sync.exchange";
    public static final String PROPERTY_SYNC_QUEUE = "property.sync.queue";
    public static final String PROPERTY_SYNC_ROUTING_KEY = "property.sync.key";

    // User data sync related
    public static final String USER_SYNC_EXCHANGE = "user.sync.exchange";
    public static final String USER_SYNC_QUEUE = "user.sync.queue";
    public static final String USER_SYNC_ROUTING_KEY = "user.sync.key";

    // Search analytics related
    public static final String SEARCH_ANALYTICS_EXCHANGE = "search.analytics.exchange";
    public static final String SEARCH_ANALYTICS_QUEUE = "search.analytics.queue";
    public static final String SEARCH_ANALYTICS_ROUTING_KEY = "search.analytics.key";

    // Property sync exchange
    @Bean
    public TopicExchange propertyExchange() {
        return new TopicExchange(PROPERTY_SYNC_EXCHANGE);
    }

    // Property sync queue
    @Bean
    public Queue propertyQueue() {
        return QueueBuilder.durable(PROPERTY_SYNC_QUEUE).build();
    }

    // Property sync binding
    @Bean
    public Binding propertyBinding() {
        return BindingBuilder
                .bind(propertyQueue())
                .to(propertyExchange())
                .with(PROPERTY_SYNC_ROUTING_KEY);
    }

    // User sync exchange
    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_SYNC_EXCHANGE);
    }

    // User sync queue
    @Bean
    public Queue userQueue() {
        return QueueBuilder.durable(USER_SYNC_QUEUE).build();
    }

    // User sync binding
    @Bean
    public Binding userBinding() {
        return BindingBuilder
                .bind(userQueue())
                .to(userExchange())
                .with(USER_SYNC_ROUTING_KEY);
    }

    // Search analytics exchange
    @Bean
    public TopicExchange searchAnalyticsExchange() {
        return new TopicExchange(SEARCH_ANALYTICS_EXCHANGE);
    }

    // Search analytics queue
    @Bean
    public Queue searchAnalyticsQueue() {
        return QueueBuilder.durable(SEARCH_ANALYTICS_QUEUE).build();
    }

    // Search analytics binding
    @Bean
    public Binding searchAnalyticsBinding() {
        return BindingBuilder
                .bind(searchAnalyticsQueue())
                .to(searchAnalyticsExchange())
                .with(SEARCH_ANALYTICS_ROUTING_KEY);
    }

    // JSON message converter
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate configuration
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
} 