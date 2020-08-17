package com.geekouc.babytun.service;

import com.geekouc.babytun.dao.GoodsCoverDAO;
import com.geekouc.babytun.dao.GoodsDAO;
import com.geekouc.babytun.dao.GoodsDetailDAO;
import com.geekouc.babytun.dao.GoodsParamDAO;
import com.geekouc.babytun.entity.Goods;
import com.geekouc.babytun.entity.GoodsCover;
import com.geekouc.babytun.entity.GoodsDetail;
import com.geekouc.babytun.entity.GoodsParam;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service //注入ioc
public class GoodsService {
    @Resource //注:使用@Autowired也可
    private GoodsDAO goodsDAO;
    @Resource
    private GoodsCoverDAO goodsCoverDAO;
    @Resource
    private GoodsDetailDAO goodsDetailDAO;
    @Resource
    private GoodsParamDAO goodsParamDAO;


    public Goods getGoods(Long goodsId){//获取商品信息
        return goodsDAO.findById(goodsId);
    }

    public List<GoodsCover> findCovers(Long goodsId){//获取商品封面
        return goodsCoverDAO.findByGoodsId(goodsId);
    }

    public List<GoodsDetail> findDetails(Long goodsId){
        return goodsDetailDAO.findByGoodsId(goodsId);
    }

    public List<GoodsParam> findParams(Long goodsId){
        return goodsParamDAO.findByGoodsId(goodsId);
    }
}
