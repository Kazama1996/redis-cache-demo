package com.kazama.redis_cache_demo.infra.lock;

import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public interface DistributeLockService {

    RLock getLock(String lockKey);


    <T> T executeWithLock (String lockKey , long waitTime , long leaseTime , TimeUnit unit , Supplier<T> supplier);

    void executeWithLock(String lockKey, long waitTime, long leaseTime,
                         TimeUnit unit, Runnable runnable);
}
