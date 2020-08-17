package com.geekouc.babytun.dao;

import com.geekouc.babytun.entity.GoodsParam;

import java.util.List;

public interface GoodsParamDAO {
    List<GoodsParam> findByGoodsId(Long goodsId);
}
