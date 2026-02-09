package com.kazama.redis_cache_demo.product.service;

import com.kazama.redis_cache_demo.infra.cache.BloomFilterService;
import com.kazama.redis_cache_demo.infra.lock.DistributeLockService;
import com.kazama.redis_cache_demo.product.dto.ProductDTO;
import com.kazama.redis_cache_demo.product.entity.Product;
import com.kazama.redis_cache_demo.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCacheService productCacheService;
    private final BloomFilterService bloomFilterService;
    private final DistributeLockService lockService;

    private static final long LOCK_WAIT_TIME = 3;  // 等待鎖的最大時間（秒）
    private static final long LOCK_LEASE_TIME = 10;  // 鎖的持有時間（秒）


    public ProductDTO getProductById(Long productId){
        log.info("QUERY product: {}" , productId);

        ProductDTO cached = productCacheService.get(productId);
        if(cached !=null){
            log.info("cache hit: {}" , productId);
            return cached;
        }

        return getProductWithLock(productId);
    }

    private ProductDTO getProductWithLock(Long productId){
        String lockKey = "product:lock:"+productId;
        RLock lock = lockService.getLock(lockKey);

        try{
            boolean acquired = lock.tryLock(LOCK_WAIT_TIME , LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if(acquired){
                try{
                    log.debug("Lock acquired success:{}" , lockKey);
                    return loadProductFromDB(productId);
                }finally {
                    lock.unlock();
                    log.debug("Release the lock: {}" ,lockKey);
                }
            }else{
                log.debug("Acquire failed, retrying: {}" , lockKey);
                Thread.sleep(100);
                ProductDTO cached = productCacheService.get(productId);
                if(cached!=null){
                    log.info("Cache hit after retry: {}" , productId);
                    return cached;
                }

                log.warn("Cache miss after retry, query from db: {}" , lockKey);
                return loadProductFromDB(productId);
            }
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            log.error("Acquired lock is interrupt: {}" ,lockKey );
            throw new RuntimeException("Query product failed" , e);
        }


    }

    private ProductDTO loadProductFromDB(Long productId){
        log.debug("query from DB");

        Product product = productRepository.findById(productId).orElse(null);

        if(product == null){
            log.warn("Product does not exists: {}",productId);
            productCacheService.setNull(productId);
            return null;
        }

        ProductDTO dto = convertToDto(product);

        productCacheService.set(productId,dto);

        log.info("Query product success from the DB then write into cache success: {}" , productId);
        return dto;

    }

    private ProductDTO convertToDto(Product product){
        return new ProductDTO(
                product.getId() ,
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getImageUrl(),
                product.getCategory(),
                product.getIsSeckill()
                );
    }
}
