package com.ordersmanagement.service;

import com.ordersmanagement.model.Order;
import com.ordersmanagement.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public Page<Order> getAllOrdersPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findAll(pageable);
    }

    public Page<Order> getOrdersByDateRange(LocalDate startDate, LocalDate endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        return orderRepository.findByOrderDateBetween(startDateTime, endDateTime, pageable);
    }

    public Page<Order> getTopOrdersByTotalAmount(int limit, int page, int size) {
        List<Order> allOrders = orderRepository.findAllOrdersByTotalAmountDesc();

        List<Order> limitedOrders = allOrders.stream()
                .limit(limit)
                .collect(Collectors.toList());
        int start = (int) Math.min(page * size, limitedOrders.size());
        int end = (int) Math.min(start + size, limitedOrders.size());

        List<Order> pageContent = limitedOrders.subList(start, end);

        return new PageImpl<>(
                pageContent,
                PageRequest.of(page, size),
                limitedOrders.size()
        );
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
    }

    public Order updateOrder(Order order) {
        Order existingOrder = getOrderById(order.getId());
        existingOrder.setProduct(order.getProduct());
        existingOrder.setOrderDate(order.getOrderDate());
        existingOrder.setQuantity(order.getQuantity());

        return orderRepository.save(existingOrder);
    }
}