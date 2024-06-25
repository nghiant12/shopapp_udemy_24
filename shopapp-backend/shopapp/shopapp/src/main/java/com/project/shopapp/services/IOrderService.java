package com.project.shopapp.services;

import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.entities.Order;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.responses.OrderResponse;

import java.util.List;

public interface IOrderService {
    OrderResponse createOrder(OrderDTO orderDTO) throws Exception;

    Order updateOrder(Long id, OrderDTO orderDTO) throws Exception;

    Order findById(Long id) throws Exception;

    void deleteOrder(Long id);

    List<Order> findByUserId(Long userId);
}
