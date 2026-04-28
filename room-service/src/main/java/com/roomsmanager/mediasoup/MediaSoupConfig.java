package com.roomsmanager.mediasoup;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;

@Configuration
public class MediaSoupConfig {

    public static final String MEDIASOUP_EVENTS_QUEUE = "mediasoup.events";
    public static final String MEDIASOUP_EVENTS_EXCHANGE = "mediasoup.events.exchange";
    public static final String MEDIASOUP_EVENTS_ROUTING_KEY = "mediasoup.events.*";

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Queue for MediaSoup events from Node.js service
     */
    @Bean
    public Queue mediasoupEventsQueue() {
        return new Queue(MEDIASOUP_EVENTS_QUEUE, true, false, false);
    }

    /**
     * Topic exchange for mediasoup events
     */
    @Bean
    public TopicExchange mediasoupEventsExchange() {
        return new TopicExchange(MEDIASOUP_EVENTS_EXCHANGE, true, false);
    }

    /**
     * Binding between queue and exchange
     */
    @Bean
    public Binding mediasoupEventsBinding(Queue mediasoupEventsQueue, TopicExchange mediasoupEventsExchange) {
        return BindingBuilder.bind(mediasoupEventsQueue)
            .to(mediasoupEventsExchange)
            .with(MEDIASOUP_EVENTS_ROUTING_KEY);
    }
}
