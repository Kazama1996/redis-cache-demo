package com.kazama.redis_cache_demo.infra.cache;

public interface BloomFilterService {

    boolean mightContainProduct(Long productId);

    void addProduct(Long productId);

    void addProducts(Iterable<Long> productIds);

    void loadAllProducts();

    void rebuild();
}
