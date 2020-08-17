package com.geekouc.babytun.controller;

import com.geekouc.babytun.service.GoodsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;

@Controller //ioc托管
public class GoodsController {
    //日志记录器;
    Logger logger = LoggerFactory.getLogger(GoodsController.class);
    @Resource
    private GoodsService goodsService;

    @GetMapping("/goods") //当访问网址为/goods时,返回goods.ftl页面
    public ModelAndView showGoods(Long gid){
        logger.info("gid:"+gid);//打印日志信息;
        ModelAndView mav = new ModelAndView("/goods");

        mav.addObject("goods",goodsService.getGoods(gid));//将查询到的goods信息放入页面中;
        mav.addObject("covers",goodsService.findCovers(gid));//将查询到的goods封面放入页面中;
        mav.addObject("details",goodsService.findDetails(gid));//将查询到的goods封面放入页面中;
        mav.addObject("params",goodsService.findParams(gid));//将查询到的goods封面放入页面中;

        return mav;
    }
}
