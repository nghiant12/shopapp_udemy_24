package com.project.shopapp.services;

import com.project.shopapp.dtos.OrderDetailDTO;
import com.project.shopapp.entities.OrderDetail;

import java.util.List;

public interface IOrderDetailService {
    OrderDetail createOrderDetail(OrderDetailDTO orderDetailDTO) throws Exception;

    OrderDetail updateOrderDetail(Long id, OrderDetailDTO newOrderDetailData) throws Exception;

    void deleteById(Long id);

    OrderDetail findById(Long id) throws Exception;

    List<OrderDetail> findByOrderId(Long orderId);
}
