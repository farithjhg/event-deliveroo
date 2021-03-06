package com.datahack.eventdeliveroo.order.infrastructure

import com.datahack.eventdeliveroo.order.domain.model.Order
import com.datahack.eventdeliveroo.order.domain.model.OrderState
import com.datahack.eventdeliveroo.order.domain.service.IOrderBuilder
import com.datahack.eventdeliveroo.order.infrastructure.kafka.IOrderProducer
import com.datahack.eventdeliveroo.order.infrastructure.mongo.OrderDas
import com.datahack.eventdeliveroo.order.infrastructure.service.OrderManager
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

class OrderManagerTest extends Specification {

    def mockedOrderBuilder = Mock(IOrderBuilder)
    def mockedOrderDas = Mock(OrderDas)
    def mockedOrderSender = Mock(IOrderProducer)
    Order order = Order.builder()
            .orderId(UUID.randomUUID().toString())
            .orderState(OrderState.ORDERED)
            .build()

    OrderManager orderService = new OrderManager(mockedOrderBuilder, mockedOrderSender, mockedOrderDas)

    def "given correct Order built and das ok response when place order then return the persisted order"() {

        given:
        mockedOrderBuilder.createOrder() >> order
        mockedOrderDas.saveOrder(order) >> Mono.just(order)

        when:
        Mono<Order> result = orderService.placeOrder()

        then:
        1 * mockedOrderSender.send(_)
        StepVerifier.create(result)
                .expectNext(order)
                .verifyComplete()
    }

    def "given Order built and das ko response when place order then exception and no message send"() {

        given:
        mockedOrderBuilder.createOrder() >> order
        mockedOrderDas.saveOrder(order) >> { throw new Exception("Duplicated Order") }

        when:
        orderService.placeOrder()

        then:
        thrown(Exception)
        0 * mockedOrderSender.send(_)
    }

    def "given null orderBuilderResult when placeOrder then exception"() {

        given:
        mockedOrderBuilder.createOrder() >> null

        when:
        orderService.placeOrder()

        then:
        thrown(Exception)
    }

    def "RetrieveOrder"() {
    }

}
