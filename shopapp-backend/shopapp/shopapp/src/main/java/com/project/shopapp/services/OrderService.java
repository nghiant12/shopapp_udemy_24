package com.project.shopapp.services;

import com.project.shopapp.dtos.CartItemDTO;
import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.entities.*;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.repositories.OrderDetailRepo;
import com.project.shopapp.repositories.OrderRepo;
import com.project.shopapp.repositories.ProductRepo;
import com.project.shopapp.repositories.UserRepo;
import com.project.shopapp.responses.OrderResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    private final OrderRepo orderRepo;
    private final UserRepo userRepo;
    private final ModelMapper modelMapper;
    private final ProductRepo productRepo;
    private final OrderDetailRepo orderDetailRepo;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderDTO orderDTO) throws Exception {
        User user = userRepo.findById(orderDTO.getUserId()).orElseThrow(() -> new DataNotFoundException("Cannot find user with id: " + orderDTO.getUserId()));
        // convert orderDTO -> order
        // use the Model Mapper library
        // create a separate mapping table stream to control mapping
        modelMapper.typeMap(OrderDTO.class, Order.class).addMappings(mapper -> mapper.skip(Order::setId));
        // update order fields from orderDTO
        Order order = modelMapper.map(orderDTO, Order.class);
        order.setUser(user);
        order.setOrderDate(new Date());
        order.setStatus(OrderStatus.PENDING);
        LocalDate shippingDate = orderDTO.getShippingDate() == null ? LocalDate.now() : orderDTO.getShippingDate();
        if (shippingDate.isBefore(LocalDate.now())) {
            throw new DataNotFoundException("Date must be at least today !");
        }
        order.setShippingDate(shippingDate);
        order.setActive(true);
        order.setTotalMoney(orderDTO.getTotalMoney());
        orderRepo.save(order);

        // create a list of OrderDetail from cartItems
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartItemDTO cartItemDTO : orderDTO.getCartItems()) {
            // create an OrderDetail from CartItemDTO
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            // get product information from cartItemDTO
            Long productId = cartItemDTO.getProductId();
            int quantity = cartItemDTO.getQuantity();
            // find product information from the database (or use cache if necessary)
            Product product = productRepo.findById(productId).orElseThrow(() -> new DataNotFoundException("Cannot find product with id: " + productId));
            // set information for OrderDetail
            orderDetail.setProduct(product);
            orderDetail.setNumberOfProducts(quantity);
            orderDetail.setPrice(product.getPrice());
            orderDetails.add(orderDetail); // add OrderDetail to the list
        }
        orderDetailRepo.saveAll(orderDetails);
        modelMapper.typeMap(Order.class, OrderResponse.class);
        return modelMapper.map(order, OrderResponse.class);
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(Long id, OrderDTO orderDTO) throws Exception {
        Order order = orderRepo.findById(id).orElseThrow(() -> new DataNotFoundException("Cannot find product with id: " + id));
        User existingUser = userRepo.findById(orderDTO.getUserId()).orElseThrow(() -> new DataNotFoundException("Cannot find user with id: " + orderDTO.getUserId()));
        modelMapper.typeMap(OrderDTO.class, Order.class).addMappings(mapper -> mapper.skip(Order::setId));
        modelMapper.map(orderDTO, order);
        order.setUser(existingUser);
        orderRepo.save(order);
        modelMapper.typeMap(Order.class, OrderResponse.class);
        return modelMapper.map(order, OrderResponse.class);
    }

    @Override
    public OrderResponse getOrder(Long id) {
        final OrderResponse orderResponse = OrderResponse.fromOrder(orderRepo.findById(id).orElse(null));
        return orderResponse;
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepo.findById(id).orElse(null);
        if (order != null) {
            order.setActive(false);
            orderRepo.save(order);
        }
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return orderRepo.findByUserId(userId);
    }
}
