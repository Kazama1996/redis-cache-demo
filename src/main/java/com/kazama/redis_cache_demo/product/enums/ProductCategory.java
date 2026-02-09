package com.kazama.redis_cache_demo.product.enums;

public enum ProductCategory {
    ELECTRONICS("3C 電子"),
    CLOTHING("服飾"),
    FOOD("食品飲料"),
    BOOKS("圖書"),
    SPORTS("運動戶外"),
    HOME("居家生活"),
    BEAUTY("美妝保養"),
    TOYS("玩具"),
    AUTOMOTIVE("汽車用品"),
    DIGITAL("數位內容");

    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

}
