package fr.insee.genesis.infrastructure.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CacheServiceAdapterTest {

    @Mock
    CacheManager cacheManager;

    @InjectMocks
    CacheServiceAdapter cacheServiceAdapter;

    @Test
    void evictCaches_test() {
        //GIVEN
        Cache mockCache = mock(Cache.class);
        String mockCacheName = "testCache";
        doReturn(mockCache).when(cacheManager).getCache(mockCacheName);
        doReturn(List.of(mockCacheName)).when(cacheManager).getCacheNames();

        //WHEN
        cacheServiceAdapter.evictCaches();

        //THEN
        verify(mockCache, times(1)).clear();
    }
}