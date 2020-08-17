package com.geekouc.babytunseckill.dao;

import com.geekouc.babytunseckill.entity.PromotionSecKill;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface PromotionSecKillDAO {
    List<PromotionSecKill> findUnstartSecKill();
    void update(PromotionSecKill promotionSecKill);
    PromotionSecKill findById(Long psId);
    List<PromotionSecKill> findExpireSecKill();
}
