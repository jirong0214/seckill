package com.geekouc.babytun.controller;

import com.geekouc.babytun.service.GoodsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;

@Controller //ioc托管
public class GoodsController {
    //日志记录器;
    Logger logger = LoggerFactory.getLogger(GoodsController.class);

    @Resource
    private GoodsService goodsService;

    @Value("${server.port}")
    private String port;


    @GetMapping("/goods-{gid}.html") //当访问网址为/goods时,返回goods.ftl页面
    public ModelAndView showGoods(@PathVariable("gid") Long gid){
        logger.info("port:"+port);//打印日志信息;
        ModelAndView mav = new ModelAndView("/goods");

        mav.addObject("goods",goodsService.getGoods(gid));//将查询到的goods信息放入页面中;
        mav.addObject("covers",goodsService.findCovers(gid));//将查询到的goods封面放入页面中;
        mav.addObject("details",goodsService.findDetails(gid));//将查询到的goods封面放入页面中;
        mav.addObject("params",goodsService.findParams(gid));//将查询到的goods封面放入页面中;

        return mav;
    }

    @GetMapping("/login")
    @ResponseBody
    public String login(String u, WebRequest request){
        request.setAttribute("user",u,WebRequest.SCOPE_SESSION); //将登陆的用户保存到session中
        return "port:"+port+",login success";
    }

    @GetMapping("/check")
    @ResponseBody
    public String checkUser(WebRequest request){  //检查用户是否已经登录
        String user = (String) request.getAttribute("user", WebRequest.SCOPE_SESSION);
        if(user != null){
            return "port:" + port + ",user=" + user;
        }else {
            return "port:" + port + ",用户未登录,redirect to login...";
        }
    }
}
