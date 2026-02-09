package com.kazama.redis_cache_demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
@Slf4j
public class RedisCacheDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedisCacheDemoApplication.class, args);
	}

	@Bean
	CommandLineRunner printConfig(Environment env,
								  @Value("${spring.datasource.url}") String dbUrl,
								  @Value("${spring.jpa.hibernate.ddl-auto}") String ddlAuto) {
		return args -> {
			log.info("========================================");
			log.info("🚀 Application Start");
			log.info("========================================");
			log.info("📌 Active Profile: {}", String.join(", ", env.getActiveProfiles()));
			log.info("========================================");
		};
	}
}
