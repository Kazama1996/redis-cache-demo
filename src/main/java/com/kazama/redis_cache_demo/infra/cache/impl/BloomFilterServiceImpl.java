package com.kazama.redis_cache_demo.infra.cache.impl;

import com.kazama.redis_cache_demo.infra.cache.BloomFilterService;
import com.kazama.redis_cache_demo.product.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BloomFilterServiceImpl implements BloomFilterService {

    private final RedissonClient redissonClient;

    private final ProductRepository productRepository;

    private static final String BLOOM_FILTER_NAME = "product:bloom:filter";

    private static final long EXPECTED_INSERTIONS = 100000;
    private static final double FALSE_POSITIVE_RATE = 0.01;

    private RBloomFilter<Long> bloomFilter;


    @PostConstruct
    public void init(){
        log.info("init the bloom filter....");

        bloomFilter = redissonClient.getBloomFilter(BLOOM_FILTER_NAME);

        if(!bloomFilter.isExists()){
            boolean initialized = bloomFilter.tryInit(EXPECTED_INSERTIONS, FALSE_POSITIVE_RATE);

            if(initialized){
                log.info("bloom filter initilization success , expect capacity: {}, false rate: {}" , EXPECTED_INSERTIONS , FALSE_POSITIVE_RATE);
                loadAllProducts();
            }else {
                log.warn("bloom filter is exists , skip the initialize");
            }
        }else {
            log.info("bloom filter is exists , current capacity: {}" , bloomFilter.count());
        }
    }


    @Override
    public boolean mightContainProduct(Long productId) {
        if(productId==null){
            return false;
        }

        boolean contains = bloomFilter.contains(productId);
        log.debug("bloom filter check:{} , result: {}" , productId , contains);
        return contains;

    }

    @Override
    public void addProduct(Long productId) {

        if(productId ==null) return ;

        bloomFilter.add(productId);
        log.debug("add product into bloom filter: {}" , productId);
    }

    @Override
    public void addProducts(Iterable<Long> productIds) {
        if(productIds==null) return;

        int count =0;
        for(Long productId : productIds){
            if(productId!=null){
                bloomFilter.add(productId);
                count++;
            }
        }

        log.debug("batch add products into bloom filter: {}" , count);
    }

    @Override
    public void loadAllProducts() {
        log.info("start to load all product IDs into bloom filter...");

        List<Long> productIds = productRepository.findAllIds();

        if(productIds.isEmpty()){
            log.warn("there is no products in DB , skip the load process");
            return;
        }

        addProducts(productIds);
        log.info("load {} product IDs into bloom filter", productIds.size());
    }

    @Override
    public void rebuild() {

        log.info("rebuild bloom filters");

        bloomFilter.delete();

        bloomFilter.tryInit(EXPECTED_INSERTIONS , FALSE_POSITIVE_RATE);

        loadAllProducts();

        log.info("rebuild bloom filter success");
    }
}
