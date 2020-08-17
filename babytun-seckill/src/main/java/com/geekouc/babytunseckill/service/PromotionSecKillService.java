package com.geekouc.babytunseckill.service;

import com.geekouc.babytunseckill.dao.OrderDAO;
import com.geekouc.babytunseckill.dao.PromotionSecKillDAO;
import com.geekouc.babytunseckill.entity.Order;
import com.geekouc.babytunseckill.entity.PromotionSecKill;
import com.geekouc.babytunseckill.service.exception.SecKillException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.UUID;

@Service
public class PromotionSecKillService {
    @Resource
    private PromotionSecKillDAO promotionSecKillDAO;
    @Resource
    private RedisTemplate<Object,Object> redisTemplate;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private OrderDAO orderDAO;

    public void processSecKill(Long psId, String userid, Integer num) throws SecKillException {
        //如果能从秒杀商品队列中获取有效的goodsId,就将psId和对应的userId放入Redis的set中;
        PromotionSecKill ps = promotionSecKillDAO.findById(psId);
        if (ps == null) {
            //秒杀活动不存在:
            throw new SecKillException("该秒杀活动不存在!");
        }
        if (ps.getStatus() == 0) {
            throw new SecKillException("该秒杀活动还未开始!");
        }
        if (ps.getStatus() == 2) {
            throw new SecKillException("该秒杀活动已结束!");
        }
        Integer goodsId = (Integer) redisTemplate.opsForList().leftPop("seckill:count:" + ps.getPsId());
        if (goodsId != null) {
            //先判断用户id的set集合中是否已经存在此id,若已存在则不允许再次抢购
            boolean isExisted = redisTemplate.opsForSet().isMember("seckill:users:" + ps.getPsId(), userid);
            if (!isExisted) {
                System.out.println("恭喜" + userid + "抢到商品了,快去下单吧!");
                redisTemplate.opsForSet().add("seckill:users:" + ps.getPsId(), userid);
            }else {
                //再将此商品加回队列的尾部
                redisTemplate.opsForList().rightPush("seckill:count:" + ps.getPsId(),ps.getGoodsId());
                throw new SecKillException("抱歉,您已经参加过此活动,请勿重复抢购!");
            }
        } else {
            throw new SecKillException("抱歉,该商品已被抢光,下次再来吧!");
        }
    }

    public String sendOrderToQueue(String userid){
        System.out.println("准备向队列发送信息...");
        //订单基本信息;
        HashMap<String,String> data = new HashMap<>();
        data.put("userid",userid);
        String orderNo = UUID.randomUUID().toString();
        data.put("orderNo",orderNo);
        //可附加额外的订单信息,如电话 地址等;
        rabbitTemplate.convertAndSend("exchange-order",null,data);
        return orderNo;
    }

    public Order checkOrder(String orderNo){
        return orderDAO.findByOrderNo(orderNo);
    }
}
