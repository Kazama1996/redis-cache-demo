package com.kazama.redis_cache_demo.product.dto;

import com.kazama.redis_cache_demo.product.enums.ProductCategory;

import java.math.BigDecimal;

public record ProductDTO(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        ProductCategory category,
        Boolean seckill
) {
}
