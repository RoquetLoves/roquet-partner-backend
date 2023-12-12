package com.lk.partner;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author lk
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.lk.partner.mapper")
public class PartnerBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(PartnerBackendApplication.class, args);
    }
}
