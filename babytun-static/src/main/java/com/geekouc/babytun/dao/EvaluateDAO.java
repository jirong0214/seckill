package com.geekouc.babytun.dao;

import com.geekouc.babytun.entity.Evaluate;

import java.util.List;

public interface EvaluateDAO {
    List<Evaluate> findByGoodsId(Long goodsId);
}
