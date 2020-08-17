package com.geekouc.babytun.dao;

import com.geekouc.babytun.entity.Goods;

public interface GoodsDAO {
    Goods findById(Long goodsId);
}
