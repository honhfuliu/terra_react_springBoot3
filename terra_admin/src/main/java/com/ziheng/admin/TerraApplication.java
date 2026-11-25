package com.ziheng.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@SpringBootApplication(scanBasePackages = "com.ziheng")
@MapperScan({"com.ziheng.system.**.mapper","com.ziheng.ai.infrastructure.**.mapper","com.ziheng.ai.capability.**.mapper"})
@EnableScheduling
public class TerraApplication {
    public static void main(String[] args) {
        SpringApplication.run(TerraApplication.class, args);
    }
}