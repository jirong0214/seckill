package com.geekouc.babytun.commons.web;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

//AOP拦截器功能:流量防刷
@Component
public class AntiRefreshInterceptor implements HandlerInterceptor {

    @Resource //RedisTemplate,用于筒化 Redis操作,在IOC容器中自动被初始化
    private RedisTemplate<Object,Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.setContentType("text/html;charset=utf-8"); //设置提示信息的字符集
        String clientIp = request.getRemoteAddr();//获取客户端IP
        String userAgent = request.getHeader("User-Agent");//获取客户端浏览器信息
        String client = "anti-refresh:" + DigestUtils.md5Hex(clientIp + "_" + userAgent);//用MD5摘要来标识一个用户

        //若此IP在黑名单中,则直接返回false;
        if(redisTemplate.hasKey("anti-refresh:blackList")){
            if(redisTemplate.opsForSet().isMember("anti-refresh:blackList",client)){
                response.getWriter().println("检测到您的IP访问异常,您已被加入黑名单!");
                return false;
            }
        }
        Integer num = (Integer) redisTemplate.opsForValue().get(client);//记录1分钟内的访问次数
        if(num == null){//第一次访问
            redisTemplate.opsForValue().set(client,1,60, TimeUnit.SECONDS); //放入redis,有效期60S
        }else{
            if(num > 20 && num < 40){
                response.getWriter().println("请求过于频繁,请1分钟后重试!");
                redisTemplate.opsForValue().increment(client,1); //每访问一次redis的值+1;
                return false;
            }else if(num >= 40){
                redisTemplate.opsForSet().add("anti-refresh:blackList",clientIp);
                response.getWriter().println("检测到您的IP访问异常,您已被加入黑名单!");
                System.out.println("IP"+clientIp+"访问异常,已被加入黑名单!");
                return false;
            }else {
                redisTemplate.opsForValue().increment(client,1); //每访问一次redis的值+1;
            }
        }
        return true;
    }
}
