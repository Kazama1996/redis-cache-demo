package com.kazama.redis_cache_demo.infra.lock.impl;

import com.kazama.redis_cache_demo.infra.lock.DistributeLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


@Slf4j
@RequiredArgsConstructor
@Service
public class DistributeLockServiceImpl implements DistributeLockService {

    private final RedissonClient redissonClient;


    @Override
    public RLock getLock(String lockKey) {
        return redissonClient.getLock(lockKey);
    }

    @Override
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Supplier<T> supplier) {
        RLock lock = getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(waitTime, leaseTime, unit);
            if (!acquired) {
                log.warn("Lock acquisition failed: {}", lockKey);
                throw new RuntimeException("Can not acquire lock" + lockKey);
            }
            log.debug("Lock acquisition success: {}", lockKey);

            try {
                return supplier.get();
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                    log.debug("Release the lock: {}", lockKey);
                }
            }
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            log.error("Lock acquire process is interrupted: {}" , lockKey , e);
            throw new RuntimeException("Lock acquire process is interrupted" , e);
        }
    }

    @Override
    public void executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Runnable runnable) {

        executeWithLock(lockKey,waitTime,leaseTime,unit, ()->{
            runnable.run();
            return null;
        });
    }
}
