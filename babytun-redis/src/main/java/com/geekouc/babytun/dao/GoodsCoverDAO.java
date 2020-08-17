package com.geekouc.babytun.dao;

import com.geekouc.babytun.entity.GoodsCover;

import java.util.List;

public interface GoodsCoverDAO {
    public List<GoodsCover> findByGoodsId(Long goodsId);
}
