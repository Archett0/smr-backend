package com.team12.notificationservice;

import com.team12.notificationservice.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RabbitMQConfigTest {

    private final RabbitMQConfig cfg = new RabbitMQConfig();

    @Test
    void notificationQueue_shouldBeDurableWithExpectedName() {
        Queue q = cfg.notificationQueue();
        assertEquals("notification.queue", q.getName());
        assertTrue(q.isDurable());
    }

    @Test
    void exchange_shouldBeDirectWithExpectedName() {
        DirectExchange ex = cfg.exchange();
        assertEquals("notification.exchange", ex.getName());
        assertEquals(ExchangeTypes.DIRECT, ex.getType());
    }

    @Test
    void binding_shouldBindQueueToExchangeWithRoutingKey() {
        Queue q = cfg.notificationQueue();
        DirectExchange ex = cfg.exchange();
        Binding b = cfg.binding(q, ex);

        assertEquals("notification.routing.key", b.getRoutingKey());
        assertEquals(q.getName(), b.getDestination());
        assertEquals(ex.getName(), b.getExchange());
        assertEquals(Binding.DestinationType.QUEUE, b.getDestinationType());
    }

    @Test
    void jsonMessageConverter_shouldUseJackson2_andSupportJavaTimeModule() {
        var conv = cfg.jsonMessageConverter();
        assertTrue(conv instanceof Jackson2JsonMessageConverter);

        assertDoesNotThrow(() -> {
            ((Jackson2JsonMessageConverter) conv)
                    .toMessage(LocalDateTime.now(), new org.springframework.amqp.core.MessageProperties());
        });
    }

    @Test
    void amqpTemplate_shouldBeRabbitTemplate_andUseOurJsonConverter() {
        ConnectionFactory cf = mock(ConnectionFactory.class);

        var tpl = cfg.amqpTemplate(cf);

        assertTrue(tpl instanceof RabbitTemplate);

        var used = ((RabbitTemplate) tpl).getMessageConverter();
        assertTrue(used instanceof Jackson2JsonMessageConverter);

        assertDoesNotThrow(() -> {
            ((Jackson2JsonMessageConverter) used)
                    .toMessage(java.time.LocalDateTime.now(), new org.springframework.amqp.core.MessageProperties());
        });
    }
}
