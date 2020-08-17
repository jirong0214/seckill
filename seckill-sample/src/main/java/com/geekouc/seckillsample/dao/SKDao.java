package com.geekouc.seckillsample.dao;



public class SKDao {
    public static Integer count = 10;
    public Integer getCount(){
        return SKDao.count;
    }

    public void updateCount(int count){
        SKDao.count = count;
    }
}
