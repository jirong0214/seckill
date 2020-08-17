package com.geekouc.babytunseckill.service;

import com.geekouc.babytunseckill.dao.OrderDAO;
import com.geekouc.babytunseckill.entity.Order;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

@Component
public class OrderConsumer {
    @Resource
    private OrderDAO orderDAO;

    @RabbitListener(    //绑定创建好的rabbitmq交换机和队列
            bindings = @QueueBinding(
                    value = @Queue(value = "queue-order"),
                    exchange = @Exchange(value = "exchange-order",type = "fanout")
            )
    )
    @RabbitHandler  //消费者获取订单数据,插入到数据库中;
    public void handleMessage(@Payload Map<String,Object> data, Channel channel,
                              @Headers Map<String,Object> headers){
        System.out.println("========获取到订单数据"+data+"========");
        try {
            try {
                //sleep 500ms,模拟对接支付、物流系统、日志登记...
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Order order = new Order();
            order.setOrderNo(data.get("orderNo").toString());
            order.setOrderStatus(0);
            order.setUserid(data.get("userid").toString());
            order.setRecvName("xxx");
            order.setRecvAddress("xxx");
            order.setRecvMobile("138********");
            order.setAmount(19.8f);
            order.setPostage(0f);
            order.setCreateTime(new Date());
            orderDAO.insert(order);//将生成的订单写入数据库中
            Long tag = (Long)headers.get(AmqpHeaders.DELIVERY_TAG);
            channel.basicAck(tag,false);//消息确认,false:只进行单个接收 不进行批量接收
            System.out.println(data.get("orderNo")+"订单已创建");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
