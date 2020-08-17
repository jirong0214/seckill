package com.geekouc.babytun.dao;

import com.geekouc.babytun.entity.GoodsDetail;

import java.util.List;

public interface GoodsDetailDAO {
    List<GoodsDetail> findByGoodsId(Long goodsId);
}
