package com.hmdp;

import com.github.benmanes.caffeine.cache.Cache;
import com.hmdp.entity.Shop;
import com.hmdp.service.impl.ShopServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.CanalClientSimulation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
class MultiLevelCacheTest {

    @Resource
    private ShopServiceImpl shopService;

    @Resource
    private CanalClientSimulation canalClientSimulation;

    @Resource
    private Cache<Long, Shop> shopCache;

    @MockBean
    private CacheClient cacheClient;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testMultiLevelCacheFlow() {
        Long shopId = 1L;
        Shop mockShop = new Shop();
        mockShop.setId(shopId);
        mockShop.setName("Test Shop");

        // 模拟 CacheClient (Redis + DB) 返回数据
        when(cacheClient.queryWithPassThrough(any(), eq(shopId), eq(Shop.class), any(), any(), any()))
                .thenReturn(mockShop);

        // 1. 第一次查询：应该未命中 Caffeine，调用 CacheClient
        System.out.println(">>> 第一次查询...");
        shopService.queryById(shopId);

        // 验证：Caffeine 中应该有数据
        Assertions.assertNotNull(shopCache.getIfPresent(shopId), "Caffeine 应该缓存了数据");
        // 验证：调用了 CacheClient
        verify(cacheClient, times(1)).queryWithPassThrough(any(), eq(shopId), eq(Shop.class), any(), any(), any());

        // 2. 第二次查询：应该命中 Caffeine，不调用 CacheClient
        System.out.println(">>> 第二次查询...");
        shopService.queryById(shopId);

        // 验证：CacheClient 调用次数依然是 1
        verify(cacheClient, times(1)).queryWithPassThrough(any(), eq(shopId), eq(Shop.class), any(), any(), any());

        // 3. 模拟 Canal 数据变更
        System.out.println(">>> 模拟 Canal 数据变更...");
        canalClientSimulation.handleDataChange(shopId);

        // 验证：Caffeine 数据被清除
        Assertions.assertNull(shopCache.getIfPresent(shopId), "Caffeine 数据应该被清除");
        // 验证：Redis 删除被调用
        verify(stringRedisTemplate, times(1)).delete(any(String.class));

        // 4. 第三次查询：应该再次调用 CacheClient
        System.out.println(">>> 第三次查询 (变更后)...");
        shopService.queryById(shopId);

        // 验证：CacheClient 调用次数变为 2
        verify(cacheClient, times(2)).queryWithPassThrough(any(), eq(shopId), eq(Shop.class), any(), any(), any());
        // 验证：Caffeine 又有了数据
        Assertions.assertNotNull(shopCache.getIfPresent(shopId));
        
        System.out.println(">>> 测试通过：多级缓存 (Caffeine -> Redis -> DB) 及 Canal 同步逻辑验证完成。");
    }
}
