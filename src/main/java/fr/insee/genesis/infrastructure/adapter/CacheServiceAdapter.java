package fr.insee.genesis.infrastructure.adapter;

import lombok.AllArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class CacheServiceAdapter {
    private final CacheManager cacheManager;

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public void evictCaches() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
    }
}
