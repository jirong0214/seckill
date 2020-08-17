package com.geekouc.seckillsample.service;

import com.geekouc.seckillsample.dao.SKDao;
import org.springframework.stereotype.Service;

@Service
public class SKService {
    private SKDao skDao = new SKDao();

    public void processSecKill(){
        Integer count = skDao.getCount();
        if(count > 0){
            System.out.println("Success!");
            count -= 1;
            skDao.updateCount(count);
        }else {
            System.out.println("Failed!");
        }
    }
}
