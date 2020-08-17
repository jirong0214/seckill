package com.geekouc.babytun;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.geekouc.babytun") //mybatis在springboot启动的时候自动扫描mybatis实现的mapper接口
public class BabytunApplication {
    public static void main(String[] args) {
        SpringApplication.run(BabytunApplication.class, args);
    }
}