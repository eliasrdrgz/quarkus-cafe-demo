package com.redhat.quarkus.cafe.domain;

import com.redhat.quarkus.cafe.infrastructure.KafkaService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@ApplicationScoped
public class Cafe {

    @Inject
    KafkaService kafkaService;

    //TODO Create and persist an Order
    public CompletableFuture<List<OrderEvent>> orderIn(CreateOrderCommand createOrderCommand) {

        List<OrderEvent> allEvents = new ArrayList<>();
        createOrderCommand.beverages.ifPresent(beverages -> {
            allEvents.addAll(createOrderCommand.beverages.get().stream().map(b -> new BeverageOrderInEvent(createOrderCommand.id, b.name, b.item)).collect(Collectors.toList()));
        });
        createOrderCommand.kitchenOrders.ifPresent(foods -> {
            allEvents.addAll(createOrderCommand.kitchenOrders.get().stream().map(f -> new KitchenOrderInEvent(createOrderCommand.id, f.name, f.item)).collect(Collectors.toList()));
        });

        return kafkaService.updateOrders(allEvents).thenApply(v -> {
            return allEvents;
        });
    }

    public CompletableFuture<Void> orderUp(OrderUpEvent orderUpEvent) {

        return kafkaService.updateUI(Arrays.asList(orderUpEvent));
    }
}
