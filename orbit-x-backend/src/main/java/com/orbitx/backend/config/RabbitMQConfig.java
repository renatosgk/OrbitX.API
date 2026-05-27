package com.orbitx.backend.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${orbitx.messaging.exchange}")
    private String exchange;

    @Value("${orbitx.messaging.alerts-queue}")
    private String alertsQueue;

    @Value("${orbitx.messaging.alerts-routing-key}")
    private String alertsRoutingKey;

    @Value("${orbitx.messaging.thermal-queue}")
    private String thermalQueue;

    @Value("${orbitx.messaging.thermal-routing-key}")
    private String thermalRoutingKey;

    private static final String DLX_EXCHANGE = "orbitx.dlx";
    private static final String DLX_QUEUE    = "orbitx.dead-letter.queue";

    @Bean
    public DirectExchange orbitXExchange() {
        return ExchangeBuilder.directExchange(exchange).durable(true).build();
    }

    @Bean
    public FanoutExchange deadLetterExchange() {
        return ExchangeBuilder.fanoutExchange(DLX_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue alertsQueue() {
        return QueueBuilder.durable(alertsQueue)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-message-ttl", 86_400_000)  
                .build();
    }

    @Bean
    public Queue thermalQueue() {
        return QueueBuilder.durable(thermalQueue)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-priority", 10)  
                .withArgument("x-message-ttl", 3_600_000)   
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLX_QUEUE).build();
    }

    @Bean
    public Binding alertsBinding() {
        return BindingBuilder.bind(alertsQueue()).to(orbitXExchange()).with(alertsRoutingKey);
    }

    @Bean
    public Binding thermalBinding() {
        return BindingBuilder.bind(thermalQueue()).to(orbitXExchange()).with(thermalRoutingKey);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange());
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setMandatory(true);
        return template;
    }
}
