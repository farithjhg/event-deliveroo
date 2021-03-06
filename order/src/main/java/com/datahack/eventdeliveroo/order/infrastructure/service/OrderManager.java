package com.datahack.eventdeliveroo.order.infrastructure.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datahack.eventdeliveroo.order.domain.model.Order;
import com.datahack.eventdeliveroo.order.domain.service.IOrderBuilder;
import com.datahack.eventdeliveroo.order.infrastructure.kafka.IOrderProducer;
import com.datahack.eventdeliveroo.order.infrastructure.mongo.OrderDas;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class OrderManager implements IOrder {

  private final IOrderBuilder orderBuilder;
  private final IOrderProducer orderSender;
  private final OrderDas orderDas;

  @Autowired
  public OrderManager(IOrderBuilder orderBuilder,
      IOrderProducer orderSender,
      OrderDas orderDas) {
    this.orderBuilder = orderBuilder;
    this.orderSender = orderSender;
    this.orderDas = orderDas;
  }

  @Override
  public Mono<Order> placeOrder() throws Exception {

    log.debug("place order");
    Order order = Optional.ofNullable(orderBuilder.createOrder())
        .orElseThrow(() -> new Exception("order must not be null"));

    Mono<Order> result = orderDas.saveOrder(order);

    orderSender.send(order);

    return result;
  }

  @Override
  public void updateOrder(Order order) {

    log.debug("update order");

    orderDas.saveOrder(order).block();
  }
}
