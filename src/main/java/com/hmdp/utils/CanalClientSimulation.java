package com.hmdp.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.hmdp.entity.Shop;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;

@Slf4j
@Component
public class CanalClientSimulation {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private Cache<Long, Shop> shopCache;

    /**
     * 模拟 Canal 监听到数据库变更后的回调
     *
     * @param shopId 变更的商铺ID
     */
    public void handleDataChange(Long shopId) {
        log.info("Canal 监听到商铺数据变更，ID: {}", shopId);

        // 1. 删除 Redis 缓存
        String key = CACHE_SHOP_KEY + shopId;
        stringRedisTemplate.delete(key);
        log.info("已删除 Redis 缓存: {}", key);

        // 2. 删除本地缓存 (Caffeine)
        shopCache.invalidate(shopId);
        log.info("已删除本地缓存 (Caffeine): {}", shopId);
    }
}
