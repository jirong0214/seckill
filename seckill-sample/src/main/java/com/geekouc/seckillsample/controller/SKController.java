package com.geekouc.seckillsample.controller;

import com.geekouc.seckillsample.service.SKService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class SKController {
    @Resource
    private SKService skService;

    @GetMapping("/kill")
    public String doSecKill(){
        skService.processSecKill();
        return "ok";
    }
}
