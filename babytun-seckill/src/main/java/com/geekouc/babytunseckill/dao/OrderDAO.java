package com.geekouc.babytunseckill.dao;

import com.geekouc.babytunseckill.entity.Order;

import java.util.List;

public interface OrderDAO {
    void insert(Order order);
    Order findByOrderNo(String orderNo);
}
