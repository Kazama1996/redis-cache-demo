package com.kazama.redis_cache_demo.infra.init.controller;

import com.kazama.redis_cache_demo.infra.init.service.DataInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dev/init")
@RequiredArgsConstructor
@Slf4j
@Profile({"dev","local"})
public class DataInitController {

    private final DataInitService dataInitService;


    @PostMapping("products/default")
    public ResponseEntity<Map<String,Object>> initDefaultProducts(){
        log.info("trigger default data initialization");

        long startTime = System.currentTimeMillis();
        dataInitService.initDefaultData();
        long duration = System.currentTimeMillis() - startTime;

        return ResponseEntity.ok(Map.of("success",true,
                "message" ,"success initialization 1000 products(10 seckill included)" ,
                "duration",duration+"ms"));

    }


    @PostMapping("/products")
    public ResponseEntity<Map<String,Object>> initProducts(
            @RequestParam(defaultValue = "1000") int total,
            @RequestParam(defaultValue = "10") int seckill){

        log.info("Trigger custom size data initialization: total={}, seckill={}" , total,seckill);

        if(seckill > total){
            return ResponseEntity.badRequest().body(Map.of(
                    "success",false,
                    "message","seckill size can not  > total size"
            ));
        }

        long startTime = System.currentTimeMillis();
        dataInitService.initProducts(total ,seckill);
        long duration = System.currentTimeMillis()-startTime;

        return ResponseEntity.ok(Map.of("success",true,
                "message" ,"success initialization" + total+ "products(" + seckill + " seckill included)" ,
                "duration",duration+"ms"));

    }

}
