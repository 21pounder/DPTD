package com.hmdp.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hmdp.entity.Shop;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineConfig {

    @Bean
    public Cache<Long, Shop> shopCache() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }
}
