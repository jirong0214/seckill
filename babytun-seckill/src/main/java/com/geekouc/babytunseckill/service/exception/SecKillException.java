package com.geekouc.babytunseckill.service.exception;

public class SecKillException extends Exception {
    //自定义一个秒杀异常类;
    public SecKillException(String msg){
        super(msg);
    }
}
