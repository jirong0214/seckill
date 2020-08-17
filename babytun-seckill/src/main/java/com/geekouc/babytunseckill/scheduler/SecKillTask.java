package com.geekouc.babytunseckill.scheduler;

import com.geekouc.babytunseckill.dao.PromotionSecKillDAO;
import com.geekouc.babytunseckill.entity.PromotionSecKill;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class SecKillTask {
    @Resource
    private PromotionSecKillDAO promotionSecKillDAO;
    @Resource
    private RedisTemplate<Object,Object> redisTemplate;
    //RedisTemplate是Spring封装的Redis操作类,提供了一系列操作redis的模板方法

    @Scheduled(cron = "0/5 * * * * ?")
    public void startSecKill(){
        List<PromotionSecKill> list = promotionSecKillDAO.findUnstartSecKill();
        for (PromotionSecKill ps : list) {
            //删除以前重复的活动任务缓存
            redisTemplate.delete("seckill:count:" + ps.getPsId());
            System.out.println(ps.getPsId() + "秒杀活动已启动 !");
            for (int i = 0; i < ps.getPsCount(); i++) {
                //有几个库存商品,则初始化几个list对象;list中先存入商品ID;
                redisTemplate.opsForList().rightPush("seckill:count:" + ps.getPsId(),ps.getGoodsId());
            }
            ps.setStatus(1);
            promotionSecKillDAO.update(ps);
        }
    }
    @Scheduled(cron = "0/5 * * * * ?")
    public void endSecKill(){
        List<PromotionSecKill> psList = promotionSecKillDAO.findExpireSecKill();
        for (PromotionSecKill ps : psList) {
            System.out.println(ps.getPsId()+"秒杀活动已结束!");
            //秒杀结束后,将此秒杀任务状态设为已过期,并更新此状态
            ps.setStatus(2);
            promotionSecKillDAO.update(ps);
            //删除redis中已过秒杀时间的商品;
            redisTemplate.delete("seckill:count" + ps.getPsId());
        }
    }
}