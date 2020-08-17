package com.geekouc.babytun.service;

import com.geekouc.babytun.dao.*;
import com.geekouc.babytun.entity.*;
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
    @Resource
    private EvaluateDAO evaluateDAO;


    public Goods getGoods(Long goodsId){//获取商品信息
        return goodsDAO.findById(goodsId);
    }

    public List<Goods> findAllGoods(){
        return goodsDAO.findAll();
    }

    public List<Goods> findLast5M(){
        return goodsDAO.findLast5M();
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

    public List<Evaluate> findEvaluates(Long goodsId){
        return evaluateDAO.findByGoodsId(goodsId);
    }
}
