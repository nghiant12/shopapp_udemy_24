package com.project.shopapp.services;

import com.project.shopapp.dtos.OrderDetailDTO;
import com.project.shopapp.entities.Order;
import com.project.shopapp.entities.OrderDetail;
import com.project.shopapp.entities.Product;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.repositories.OrderDetailRepo;
import com.project.shopapp.repositories.OrderRepo;
import com.project.shopapp.repositories.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderDetailService implements IOrderDetailService {
    private final OrderDetailRepo orderDetailRepo;
    private final OrderRepo orderRepo;
    private final ProductRepo productRepo;

    @Override
    public OrderDetail createOrderDetail(OrderDetailDTO orderDetailDTO) throws Exception {
        Order order = orderRepo.findById(orderDetailDTO.getOrderId())
                .orElseThrow(() -> new DataNotFoundException("Order not found"));
        Product product = productRepo.findById(orderDetailDTO.getProductId())
                .orElseThrow(() -> new DataNotFoundException("Product not found"));
        OrderDetail orderDetail = OrderDetail.builder()
                .order(order)
                .product(product)
                .price(orderDetailDTO.getPrice())
                .numberOfProducts(orderDetailDTO.getNumberOfProducts())
                .totalMoney(orderDetailDTO.getTotalMoney())
                .color(orderDetailDTO.getColor())
                .build();
        return orderDetailRepo.save(orderDetail);
    }

    @Override
    public OrderDetail updateOrderDetail(Long id, OrderDetailDTO orderDetailDTO) throws Exception {
        OrderDetail existingOrderDetail = findById(id);
        Order existingOrder = orderRepo.findById(orderDetailDTO.getOrderId())
                .orElseThrow(() -> new DataNotFoundException("Order not found"));
        Product existingProduct = productRepo.findById(orderDetailDTO.getProductId())
                .orElseThrow(() -> new DataNotFoundException("Product not found"));
        existingOrderDetail.setOrder(existingOrder);
        existingOrderDetail.setProduct(existingProduct);
        existingOrderDetail.setPrice(orderDetailDTO.getPrice());
        existingOrderDetail.setNumberOfProducts(orderDetailDTO.getNumberOfProducts());
        existingOrderDetail.setTotalMoney(orderDetailDTO.getTotalMoney());
        existingOrderDetail.setColor(orderDetailDTO.getColor());
        return orderDetailRepo.save(existingOrderDetail);
    }

    @Override
    public void deleteById(Long id) {
        orderDetailRepo.deleteById(id);
    }

    @Override
    public OrderDetail findById(Long id) throws Exception {
        return orderDetailRepo.findById(id).orElseThrow(() -> new DataNotFoundException("Order detail not found"));
    }

    @Override
    public List<OrderDetail> findByOrderId(Long orderId) {
        return orderDetailRepo.findByOrderId(orderId);
    }
}
