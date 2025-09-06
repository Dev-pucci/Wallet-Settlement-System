package com.wallet.config;

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

    @Value("${wallet.rabbitmq.exchanges.wallet-exchange}")
    private String walletExchange;

    @Value("${wallet.rabbitmq.queues.transaction-processing}")
    private String transactionQueue;

    @Value("${wallet.rabbitmq.queues.reconciliation-processing}")
    private String reconciliationQueue;

    @Value("${wallet.rabbitmq.routing-keys.transaction}")
    private String transactionRoutingKey;

    @Value("${wallet.rabbitmq.routing-keys.reconciliation}")
    private String reconciliationRoutingKey;

    @Bean
    public TopicExchange walletExchange() {
        return new TopicExchange(walletExchange);
    }

    @Bean
    public Queue transactionQueue() {
        return QueueBuilder.durable(transactionQueue)
                .withArgument("x-dead-letter-exchange", walletExchange + ".dlx")
                .withArgument("x-dead-letter-routing-key", "dead.transaction")
                .build();
    }

    @Bean
    public Queue reconciliationQueue() {
        return QueueBuilder.durable(reconciliationQueue)
                .withArgument("x-dead-letter-exchange", walletExchange + ".dlx")
                .withArgument("x-dead-letter-routing-key", "dead.reconciliation")
                .build();
    }

    @Bean
    public Queue transactionDeadLetterQueue() {
        return QueueBuilder.durable(transactionQueue + ".dlq").build();
    }

    @Bean
    public Queue reconciliationDeadLetterQueue() {
        return QueueBuilder.durable(reconciliationQueue + ".dlq").build();
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(walletExchange + ".dlx");
    }

    @Bean
    public Binding transactionBinding() {
        return BindingBuilder
                .bind(transactionQueue())
                .to(walletExchange())
                .with(transactionRoutingKey);
    }

    @Bean
    public Binding reconciliationBinding() {
        return BindingBuilder
                .bind(reconciliationQueue())
                .to(walletExchange())
                .with(reconciliationRoutingKey);
    }

    @Bean
    public Binding transactionDeadLetterBinding() {
        return BindingBuilder
                .bind(transactionDeadLetterQueue())
                .to(deadLetterExchange())
                .with("dead.transaction");
    }

    @Bean
    public Binding reconciliationDeadLetterBinding() {
        return BindingBuilder
                .bind(reconciliationDeadLetterQueue())
                .to(deadLetterExchange())
                .with("dead.reconciliation");
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
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.err.println("Message not delivered: " + cause);
            }
        });
        template.setReturnsCallback(returned -> {
            System.err.println("Message returned: " + returned.getMessage());
        });
        return template;
    }
}