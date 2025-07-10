package com.winnguyen1905.order.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.winnguyen1905.order.model.event.OrderCreatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

  private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

  @Value("${topic.name.order.created:order-created}")
  private String orderCreatedTopic;

  public void publishOrderCreated(OrderCreatedEvent event) {
    log.info("Publishing OrderCreated event: orderId={}, sagaId={}",
        event.getOrderId(), event.getSagaId());

    try {
      kafkaTemplate.send(orderCreatedTopic, event.getOrderId().toString(), event);
      log.info("Successfully published OrderCreated event for orderId: {}", event.getOrderId());
    } catch (Exception e) {
      log.error("Failed to publish OrderCreated event for orderId: {}", event.getOrderId(), e);
      throw new RuntimeException("Failed to publish order created event", e);
    }
  }
}
