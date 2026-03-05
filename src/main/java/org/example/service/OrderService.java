package org.example.service;


import org.example.entity.Order;
import org.example.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository repository;

    // Simulate a migration flag (In production, this could be in Redis/Config Server)
    private boolean isMigrationActive = true;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Order saveOrder(Order order) {
        // 1. Save to the current shard (Primary Write)
        Order savedOrder = repository.save(order);

        // 2. ZERO DOWNTIME MIGRATION LOGIC
        // If we are adding a 3rd shard, we "Dual Write" to the Migration Log
        if (isMigrationActive) {
            simulateMigrationLog(savedOrder);
        }

        return savedOrder;
    }

    private void simulateMigrationLog(Order order) {
        log.info("🚀 [MIGRATION LOG]: Dual-writing Order ID {} for User {} to New Shard (ds2)",
                order.getId(), order.getUserId());
        // In a real system, this would be: migrationProducer.send(order);
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return repository.findByUserId(userId);
    }
}