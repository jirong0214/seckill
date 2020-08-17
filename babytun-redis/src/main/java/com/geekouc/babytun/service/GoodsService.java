package com.geekouc.babytun.service;

import com.geekouc.babytun.dao.GoodsCoverDAO;
import com.geekouc.babytun.dao.GoodsDAO;
import com.geekouc.babytun.dao.GoodsDetailDAO;
import com.geekouc.babytun.dao.GoodsParamDAO;
import com.geekouc.babytun.entity.Goods;
import com.geekouc.babytun.entity.GoodsCover;
import com.geekouc.babytun.entity.GoodsDetail;
import com.geekouc.babytun.entity.GoodsParam;
import org.springframework.cache.annotation.Cacheable;
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

    //第一次访问的时候将方法的返回结果放入缓存
    //第二次访问时不再执行方法内部的代码,而是从缓存中提取数据!
    //存储缓存的形式: goods::1,goods::2....
    @Cacheable(value = "goods",key = "#goodsId")
    public Goods getGoods(Long goodsId){//获取商品信息
        return goodsDAO.findById(goodsId);
    }

    @Cacheable(value = "covers",key = "#goodsId") //covers::1..
    public List<GoodsCover> findCovers(Long goodsId){//获取商品封面
        return goodsCoverDAO.findByGoodsId(goodsId);
    }

    @Cacheable(value = "details",key = "#goodsId")
    public List<GoodsDetail> findDetails(Long goodsId){
        return goodsDetailDAO.findByGoodsId(goodsId);
    }

    @Cacheable(value = "params",key = "#goodsId")
    public List<GoodsParam> findParams(Long goodsId){
        return goodsParamDAO.findByGoodsId(goodsId);
    }
}
