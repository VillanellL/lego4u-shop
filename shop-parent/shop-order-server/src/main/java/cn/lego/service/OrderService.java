package cn.lego.service;

import cn.lego.domian.Order;

public interface OrderService {
    public Order createOrder(Long productId, Long userId);
}
