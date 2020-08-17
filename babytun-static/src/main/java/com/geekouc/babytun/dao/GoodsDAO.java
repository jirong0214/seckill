package com.geekouc.babytun.dao;

import com.geekouc.babytun.entity.Goods;

import java.util.List;

public interface GoodsDAO {
    Goods findById(Long goodsId);
    List<Goods> findAll();
    List<Goods> findLast5M();
}
