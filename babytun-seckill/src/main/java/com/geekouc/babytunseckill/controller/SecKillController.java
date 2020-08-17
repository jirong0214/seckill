package com.geekouc.babytunseckill.controller;

import com.geekouc.babytunseckill.entity.Order;
import com.geekouc.babytunseckill.service.PromotionSecKillService;
import com.geekouc.babytunseckill.service.exception.SecKillException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SecKillController {
    @Resource
    PromotionSecKillService promotionSecKillService;

    @RequestMapping("/seckill")
    public Map<String,Object> processSecKill(Long psid, String userid) {
        Map<String, Object> result = new HashMap<>();
        try {
            promotionSecKillService.processSecKill(psid, userid, 1);
            //生产者:将订单放入队列中
            String orderNo = promotionSecKillService.sendOrderToQueue(userid);
            HashMap<String,String> data = new HashMap<>();
            data.put("orderNo",orderNo); //生成订单编号
            result.put("code", "0");
            result.put("message", "success");
            result.put("data",data);
        } catch (SecKillException e) {
            result.put("code", "500");
            result.put("message", e.getMessage());
        }
        return result;
    }

    //检查订单号是否已经在数据库中成功生成
    @GetMapping("/checkorder")
    public ModelAndView checkOrder(String orderNo){
        ModelAndView mav = new ModelAndView();
        Order order = promotionSecKillService.checkOrder(orderNo);
        if(order != null){
            //代表订单已在数据库中创建好了
            mav.addObject("order",order);
            mav.setViewName("/order");
            //跳转到order页面,显示订单信息;
        }else{
            mav.addObject("orderNo",orderNo);
            mav.setViewName("/waiting");
            //跳转到等待页面,只显示订单号;
        }
        return mav;
    }
}
